package com.contracthawk.controller;

import com.contracthawk.dto.*;
import com.contracthawk.entity.Lifecycle;
import com.contracthawk.service.ApiContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/apis")
@RequiredArgsConstructor
@Tag(name = "API Contracts", description = "API contract management endpoints")
public class ApiContractController {
    
    private final ApiContractService apiContractService;
    
    @GetMapping
    @Operation(summary = "List all API contracts", description = "Search, filter and sort API contracts")
    public ResponseEntity<List<ApiSummaryDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Lifecycle lifecycle,
            @RequestParam(required = false) String ownerTeam,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir) {
        
        List<ApiSummaryDto> apis = apiContractService.findAll(q, lifecycle, ownerTeam, sort, dir);
        return ResponseEntity.ok(apis);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get API contract details", description = "Get full details including endpoints and changelog")
    public ResponseEntity<ApiDetailDto> getById(@PathVariable UUID id) {
        ApiDetailDto api = apiContractService.findById(id);
        return ResponseEntity.ok(api);
    }
    
    @PostMapping
    @Operation(summary = "Create new API contract")
    public ResponseEntity<ApiDetailDto> create(@Valid @RequestBody ApiCreateDto dto) {
        ApiDetailDto created = apiContractService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update API contract")
    public ResponseEntity<ApiDetailDto> update(@PathVariable UUID id, @Valid @RequestBody ApiUpdateDto dto) {
        ApiDetailDto updated = apiContractService.update(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{id}/lifecycle")
    @Operation(summary = "Update API contract lifecycle")
    public ResponseEntity<Void> updateLifecycle(@PathVariable UUID id, @Valid @RequestBody LifecyclePatchDto dto) {
        apiContractService.updateLifecycle(id, dto.getLifecycle());
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete API contract")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        apiContractService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/endpoints")
    @Operation(summary = "Add endpoint to API contract")
    public ResponseEntity<EndpointDto> addEndpoint(@PathVariable UUID id, @Valid @RequestBody EndpointCreateDto dto) {
        EndpointDto endpoint = apiContractService.addEndpoint(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(endpoint);
    }
    
    @PutMapping("/{id}/endpoints/{endpointId}")
    @Operation(summary = "Update endpoint")
    public ResponseEntity<EndpointDto> updateEndpoint(
            @PathVariable UUID id,
            @PathVariable UUID endpointId,
            @Valid @RequestBody EndpointCreateDto dto) {
        EndpointDto endpoint = apiContractService.updateEndpoint(id, endpointId, dto);
        return ResponseEntity.ok(endpoint);
    }
    
    @DeleteMapping("/{id}/endpoints/{endpointId}")
    @Operation(summary = "Delete endpoint")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable UUID id, @PathVariable UUID endpointId) {
        apiContractService.deleteEndpoint(id, endpointId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/changelog")
    @Operation(summary = "Add changelog entry")
    public ResponseEntity<ChangelogDto> addChangelogEntry(@PathVariable UUID id, @Valid @RequestBody ChangelogCreateDto dto) {
        ChangelogDto entry = apiContractService.addChangelogEntry(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }
    
    @DeleteMapping("/{id}/changelog/{entryId}")
    @Operation(summary = "Delete changelog entry")
    public ResponseEntity<Void> deleteChangelogEntry(@PathVariable UUID id, @PathVariable UUID entryId) {
        apiContractService.deleteChangelogEntry(id, entryId);
        return ResponseEntity.noContent().build();
    }
}

