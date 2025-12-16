package com.contracthawk.service;

import com.contracthawk.dto.*;
import com.contracthawk.entity.*;
import com.contracthawk.exception.ResourceNotFoundException;
import com.contracthawk.mapper.ApiContractMapper;
import com.contracthawk.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenApiSyncService {
    
    private final ApiContractRepository apiContractRepository;
    private final EndpointRepository endpointRepository;
    private final ApiSyncRunRepository apiSyncRunRepository;
    private final ApiBreakingChangeRepository apiBreakingChangeRepository;
    private final ChangelogEntryRepository changelogEntryRepository;
    private final OpenApiService openApiService;
    private final ApiContractMapper mapper;
    
    @Transactional
    public ImportResultDto importOpenApi(UUID apiId, SyncMode mode) {
        ApiContract api = apiContractRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + apiId));
        
        if (api.getOpenApiUrl() == null || api.getOpenApiUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("API contract does not have an openApiUrl configured");
        }
        
        ApiSyncRun syncRun = ApiSyncRun.builder()
                .apiContract(api)
                .runAt(Instant.now())
                .mode(mode)
                .status(SyncStatus.FAILED)
                .addedCount(0)
                .updatedCount(0)
                .deletedCount(0)
                .breaksDetected(false)
                .build();
        
        try {
            // Fetch and parse OpenAPI spec
            List<Endpoint> specEndpoints = openApiService.parseOpenApiSpec(api.getOpenApiUrl());
            
            // Get current endpoints
            List<Endpoint> currentEndpoints = endpointRepository.findByApiContractId(apiId);
            
            // Perform sync based on mode
            SyncResult result = mode == SyncMode.REPLACE 
                    ? performReplace(currentEndpoints, specEndpoints, api)
                    : performMerge(currentEndpoints, specEndpoints, api);
            
            // Save sync run
            syncRun.setStatus(SyncStatus.SUCCESS);
            syncRun.setAddedCount(result.addedCount);
            syncRun.setUpdatedCount(result.updatedCount);
            syncRun.setDeletedCount(result.deletedCount);
            syncRun.setBreaksDetected(result.breaksDetected);
            syncRun = apiSyncRunRepository.save(syncRun);
            
            // Save breaking changes
            if (result.breaksDetected && !result.breakingChanges.isEmpty()) {
                for (BreakingChangeInfo bc : result.breakingChanges) {
                    ApiBreakingChange breakingChange = ApiBreakingChange.builder()
                            .syncRun(syncRun)
                            .type(bc.type)
                            .method(bc.method)
                            .path(bc.path)
                            .details(bc.details)
                            .build();
                    apiBreakingChangeRepository.save(breakingChange);
                }
            }
            
            // Create auto-changelog entry if there are changes
            if (result.addedCount > 0 || result.updatedCount > 0 || result.deletedCount > 0) {
                createAutoChangelogEntry(api, result, result.breaksDetected);
            }
            
            // Build response
            List<BreakingChangeDto> breakingChangeDtos = result.breakingChanges.stream()
                    .map(bc -> BreakingChangeDto.builder()
                            .type(bc.type)
                            .method(bc.method)
                            .path(bc.path)
                            .details(bc.details)
                            .build())
                    .collect(Collectors.toList());
            
            return ImportResultDto.builder()
                    .syncRunId(syncRun.getId())
                    .addedCount(result.addedCount)
                    .updatedCount(result.updatedCount)
                    .deletedCount(result.deletedCount)
                    .breaksDetected(result.breaksDetected)
                    .breakingChanges(breakingChangeDtos)
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to import OpenAPI for API {}: {}", apiId, e.getMessage(), e);
            syncRun.setErrorMessage(e.getMessage() != null && e.getMessage().length() > 500 
                    ? e.getMessage().substring(0, 500) 
                    : e.getMessage());
            apiSyncRunRepository.save(syncRun);
            throw new RuntimeException("Failed to import OpenAPI: " + e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public DiffDto previewDiff(UUID apiId) {
        ApiContract api = apiContractRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + apiId));
        
        if (api.getOpenApiUrl() == null || api.getOpenApiUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("API contract does not have an openApiUrl configured");
        }
        
        // Fetch and parse OpenAPI spec
        List<Endpoint> specEndpoints = openApiService.parseOpenApiSpec(api.getOpenApiUrl());
        
        // Get current endpoints
        List<Endpoint> currentEndpoints = endpointRepository.findByApiContractId(apiId);
        
        // Build maps for comparison
        Map<String, Endpoint> currentMap = currentEndpoints.stream()
                .collect(Collectors.toMap(
                        e -> e.getMethod().name() + " " + e.getPath(),
                        e -> e
                ));
        
        Map<String, Endpoint> specMap = specEndpoints.stream()
                .collect(Collectors.toMap(
                        e -> e.getMethod().name() + " " + e.getPath(),
                        e -> e
                ));
        
        // Find added endpoints
        List<EndpointDto> added = specEndpoints.stream()
                .filter(e -> !currentMap.containsKey(e.getMethod().name() + " " + e.getPath()))
                .map(mapper::toEndpointDto)
                .collect(Collectors.toList());
        
        // Find removed endpoints
        List<EndpointDto> removed = currentEndpoints.stream()
                .filter(e -> !specMap.containsKey(e.getMethod().name() + " " + e.getPath()))
                .map(mapper::toEndpointDto)
                .collect(Collectors.toList());
        
        // Find changed endpoints
        List<ChangedEndpointDto> changed = new ArrayList<>();
        for (Endpoint specEndpoint : specEndpoints) {
            String key = specEndpoint.getMethod().name() + " " + specEndpoint.getPath();
            Endpoint currentEndpoint = currentMap.get(key);
            
            if (currentEndpoint != null) {
                boolean hasChanges = false;
                List<String> changes = new ArrayList<>();
                
                if (!Objects.equals(currentEndpoint.getDescription(), specEndpoint.getDescription())) {
                    hasChanges = true;
                    changes.add("description");
                }
                if (!Objects.equals(currentEndpoint.getDeprecated(), specEndpoint.getDeprecated())) {
                    hasChanges = true;
                    changes.add("deprecated");
                }
                
                if (hasChanges) {
                    changed.add(ChangedEndpointDto.builder()
                            .current(mapper.toEndpointDto(currentEndpoint))
                            .proposed(mapper.toEndpointDto(specEndpoint))
                            .changeDescription("Changed: " + String.join(", ", changes))
                            .build());
                }
            }
        }
        
        return DiffDto.builder()
                .addedEndpoints(added)
                .removedEndpoints(removed)
                .changedEndpoints(changed)
                .build();
    }
    
    @Transactional
    public RefreshSummaryDto refreshAll(SyncMode defaultMode) {
        List<ApiContract> apisWithOpenApi = apiContractRepository.findAll().stream()
                .filter(api -> api.getOpenApiUrl() != null && !api.getOpenApiUrl().trim().isEmpty())
                .collect(Collectors.toList());
        
        int succeeded = 0;
        int failed = 0;
        List<RefreshFailureDto> failures = new ArrayList<>();
        
        for (ApiContract api : apisWithOpenApi) {
            try {
                importOpenApi(api.getId(), defaultMode);
                succeeded++;
            } catch (Exception e) {
                failed++;
                failures.add(RefreshFailureDto.builder()
                        .apiId(api.getId())
                        .apiName(api.getName())
                        .reason(e.getMessage() != null && e.getMessage().length() > 200 
                                ? e.getMessage().substring(0, 200) 
                                : e.getMessage())
                        .build());
                log.error("Failed to refresh API {}: {}", api.getId(), e.getMessage(), e);
            }
        }
        
        return RefreshSummaryDto.builder()
                .totalApis(apisWithOpenApi.size())
                .succeeded(succeeded)
                .failed(failed)
                .failures(failures)
                .build();
    }
    
    private SyncResult performMerge(List<Endpoint> current, List<Endpoint> spec, ApiContract api) {
        Map<String, Endpoint> currentMap = current.stream()
                .collect(Collectors.toMap(
                        e -> e.getMethod().name() + " " + e.getPath(),
                        e -> e
                ));
        
        int added = 0;
        int updated = 0;
        List<BreakingChangeInfo> breakingChanges = new ArrayList<>();
        
        for (Endpoint specEndpoint : spec) {
            String key = specEndpoint.getMethod().name() + " " + specEndpoint.getPath();
            Endpoint existing = currentMap.get(key);
            
            if (existing == null) {
                // Add new endpoint
                specEndpoint.setApiContract(api);
                endpointRepository.save(specEndpoint);
                added++;
            } else {
                // Update existing if changed
                boolean changed = false;
                if (!Objects.equals(existing.getDescription(), specEndpoint.getDescription())) {
                    existing.setDescription(specEndpoint.getDescription());
                    changed = true;
                }
                if (!Objects.equals(existing.getDeprecated(), specEndpoint.getDeprecated())) {
                    existing.setDeprecated(specEndpoint.getDeprecated());
                    changed = true;
                }
                if (changed) {
                    endpointRepository.save(existing);
                    updated++;
                }
            }
        }
        
        return new SyncResult(added, updated, 0, false, breakingChanges);
    }
    
    private SyncResult performReplace(List<Endpoint> current, List<Endpoint> spec, ApiContract api) {
        Map<String, Endpoint> currentMap = current.stream()
                .collect(Collectors.toMap(
                        e -> e.getMethod().name() + " " + e.getPath(),
                        e -> e
                ));
        
        Map<String, Endpoint> specMap = spec.stream()
                .collect(Collectors.toMap(
                        e -> e.getMethod().name() + " " + e.getPath(),
                        e -> e
                ));
        
        int added = 0;
        int updated = 0;
        int deleted = 0;
        List<BreakingChangeInfo> breakingChanges = new ArrayList<>();
        
        // Add/update endpoints from spec
        for (Endpoint specEndpoint : spec) {
            String key = specEndpoint.getMethod().name() + " " + specEndpoint.getPath();
            Endpoint existing = currentMap.get(key);
            
            if (existing == null) {
                specEndpoint.setApiContract(api);
                endpointRepository.save(specEndpoint);
                added++;
            } else {
                boolean changed = false;
                if (!Objects.equals(existing.getDescription(), specEndpoint.getDescription())) {
                    existing.setDescription(specEndpoint.getDescription());
                    changed = true;
                }
                if (!Objects.equals(existing.getDeprecated(), specEndpoint.getDeprecated())) {
                    existing.setDeprecated(specEndpoint.getDeprecated());
                    changed = true;
                }
                if (changed) {
                    endpointRepository.save(existing);
                    updated++;
                }
            }
        }
        
        // Remove endpoints not in spec
        for (Endpoint currentEndpoint : current) {
            String key = currentEndpoint.getMethod().name() + " " + currentEndpoint.getPath();
            if (!specMap.containsKey(key)) {
                breakingChanges.add(new BreakingChangeInfo(
                        BreakingChangeType.REMOVED_ENDPOINT,
                        currentEndpoint.getMethod().name(),
                        currentEndpoint.getPath(),
                        "Endpoint removed from OpenAPI spec"
                ));
                endpointRepository.delete(currentEndpoint);
                deleted++;
            }
        }
        
        boolean breaksDetected = deleted > 0;
        
        return new SyncResult(added, updated, deleted, breaksDetected, breakingChanges);
    }
    
    private void createAutoChangelogEntry(ApiContract api, SyncResult result, boolean breaking) {
        StringBuilder summary = new StringBuilder("Synced from OpenAPI: ");
        List<String> parts = new ArrayList<>();
        if (result.addedCount > 0) parts.add("+" + result.addedCount);
        if (result.updatedCount > 0) parts.add("~" + result.updatedCount);
        if (result.deletedCount > 0) parts.add("-" + result.deletedCount);
        summary.append(String.join(" ", parts));
        
        StringBuilder details = new StringBuilder();
        if (result.addedCount > 0) details.append("Added: ").append(result.addedCount).append(" endpoints. ");
        if (result.updatedCount > 0) details.append("Updated: ").append(result.updatedCount).append(" endpoints. ");
        if (result.deletedCount > 0) details.append("Removed: ").append(result.deletedCount).append(" endpoints. ");
        
        if (details.length() > 1000) {
            details = new StringBuilder(details.substring(0, 997) + "...");
        }
        
        ChangelogEntry entry = ChangelogEntry.builder()
                .apiContract(api)
                .type(ChangelogType.CHANGED)
                .breaking(breaking)
                .summary(summary.toString())
                .details(details.toString())
                .releasedAt(Instant.now())
                .build();
        
        changelogEntryRepository.save(entry);
    }
    
    private static class SyncResult {
        final int addedCount;
        final int updatedCount;
        final int deletedCount;
        final boolean breaksDetected;
        final List<BreakingChangeInfo> breakingChanges;
        
        SyncResult(int addedCount, int updatedCount, int deletedCount, boolean breaksDetected, 
                  List<BreakingChangeInfo> breakingChanges) {
            this.addedCount = addedCount;
            this.updatedCount = updatedCount;
            this.deletedCount = deletedCount;
            this.breaksDetected = breaksDetected;
            this.breakingChanges = breakingChanges;
        }
    }
    
    private static class BreakingChangeInfo {
        final BreakingChangeType type;
        final String method;
        final String path;
        final String details;
        
        BreakingChangeInfo(BreakingChangeType type, String method, String path, String details) {
            this.type = type;
            this.method = method;
            this.path = path;
            this.details = details;
        }
    }
}

