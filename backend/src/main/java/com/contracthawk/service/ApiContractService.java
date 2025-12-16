package com.contracthawk.service;

import com.contracthawk.dto.*;
import com.contracthawk.entity.ApiContract;
import com.contracthawk.entity.ChangelogEntry;
import com.contracthawk.entity.Endpoint;
import com.contracthawk.entity.Lifecycle;
import com.contracthawk.exception.ResourceNotFoundException;
import com.contracthawk.exception.DuplicateResourceException;
import com.contracthawk.mapper.ApiContractMapper;
import com.contracthawk.repository.ApiContractRepository;
import com.contracthawk.repository.ChangelogEntryRepository;
import com.contracthawk.repository.EndpointRepository;
import com.contracthawk.specification.ApiContractSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiContractService {
    
    private final ApiContractRepository apiContractRepository;
    private final EndpointRepository endpointRepository;
    private final ChangelogEntryRepository changelogEntryRepository;
    private final ApiContractMapper mapper;
    
    @Transactional(readOnly = true)
    public List<ApiSummaryDto> findAll(String q, Lifecycle lifecycle, String ownerTeam, String sort, String dir) {
        Specification<ApiContract> spec = ApiContractSpecification.search(q, lifecycle, ownerTeam);
        
        Sort sortObj = buildSort(sort, dir);
        List<ApiContract> entities = apiContractRepository.findAll(spec, sortObj);
        
        return entities.stream()
                .map(mapper::toSummaryDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ApiDetailDto findById(UUID id) {
        ApiContract entity = apiContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + id));
        
        return mapper.toDetailDto(entity);
    }
    
    @Transactional
    public ApiDetailDto create(ApiCreateDto dto) {
        // Check unique constraint: name + ownerTeam
        if (apiContractRepository.findByNameAndOwnerTeam(dto.getName(), dto.getOwnerTeam()).isPresent()) {
            throw new DuplicateResourceException("API contract with name '" + dto.getName() + 
                    "' already exists for team '" + dto.getOwnerTeam() + "'");
        }
        
        ApiContract entity = mapper.toEntity(dto);
        entity = apiContractRepository.save(entity);
        return mapper.toDetailDto(entity);
    }
    
    @Transactional
    public ApiDetailDto update(UUID id, ApiUpdateDto dto) {
        ApiContract entity = apiContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + id));
        
        // Check unique constraint if name or ownerTeam changed
        if (!entity.getName().equals(dto.getName()) || !entity.getOwnerTeam().equals(dto.getOwnerTeam())) {
            if (apiContractRepository.findByNameAndOwnerTeam(dto.getName(), dto.getOwnerTeam()).isPresent()) {
                throw new DuplicateResourceException("API contract with name '" + dto.getName() + 
                        "' already exists for team '" + dto.getOwnerTeam() + "'");
            }
        }
        
        mapper.updateEntity(entity, dto);
        entity = apiContractRepository.save(entity);
        return mapper.toDetailDto(entity);
    }
    
    @Transactional
    public void updateLifecycle(UUID id, Lifecycle lifecycle) {
        ApiContract entity = apiContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + id));
        
        entity.setLifecycle(lifecycle);
        apiContractRepository.save(entity);
    }
    
    @Transactional
    public void delete(UUID id) {
        ApiContract entity = apiContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + id));
        
        apiContractRepository.delete(entity);
    }
    
    @Transactional
    public EndpointDto addEndpoint(UUID apiId, EndpointCreateDto dto) {
        ApiContract api = apiContractRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + apiId));
        
        // Check unique constraint: method + path per API
        if (endpointRepository.existsByApiContractIdAndMethodAndPath(apiId, dto.getMethod(), dto.getPath())) {
            throw new DuplicateResourceException("Endpoint with method '" + dto.getMethod() + 
                    "' and path '" + dto.getPath() + "' already exists for this API");
        }
        
        Endpoint endpoint = Endpoint.builder()
                .apiContract(api)
                .method(dto.getMethod())
                .path(dto.getPath())
                .description(dto.getDescription())
                .deprecated(dto.getDeprecated() != null ? dto.getDeprecated() : false)
                .build();
        
        endpoint = endpointRepository.save(endpoint);
        return mapper.toEndpointDto(endpoint);
    }
    
    @Transactional
    public EndpointDto updateEndpoint(UUID apiId, UUID endpointId, EndpointCreateDto dto) {
        ApiContract api = apiContractRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + apiId));
        
        Endpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint not found with id: " + endpointId));
        
        if (!endpoint.getApiContract().getId().equals(apiId)) {
            throw new ResourceNotFoundException("Endpoint does not belong to this API");
        }
        
        // Check unique constraint if method or path changed
        if (!endpoint.getMethod().equals(dto.getMethod()) || !endpoint.getPath().equals(dto.getPath())) {
            if (endpointRepository.existsByApiContractIdAndMethodAndPath(apiId, dto.getMethod(), dto.getPath())) {
                throw new DuplicateResourceException("Endpoint with method '" + dto.getMethod() + 
                        "' and path '" + dto.getPath() + "' already exists for this API");
            }
        }
        
        endpoint.setMethod(dto.getMethod());
        endpoint.setPath(dto.getPath());
        endpoint.setDescription(dto.getDescription());
        endpoint.setDeprecated(dto.getDeprecated() != null ? dto.getDeprecated() : false);
        
        endpoint = endpointRepository.save(endpoint);
        return mapper.toEndpointDto(endpoint);
    }
    
    @Transactional
    public void deleteEndpoint(UUID apiId, UUID endpointId) {
        ApiContract api = apiContractRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + apiId));
        
        Endpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint not found with id: " + endpointId));
        
        if (!endpoint.getApiContract().getId().equals(apiId)) {
            throw new ResourceNotFoundException("Endpoint does not belong to this API");
        }
        
        endpointRepository.delete(endpoint);
    }
    
    @Transactional
    public ChangelogDto addChangelogEntry(UUID apiId, ChangelogCreateDto dto) {
        ApiContract api = apiContractRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + apiId));
        
        ChangelogEntry entry = ChangelogEntry.builder()
                .apiContract(api)
                .type(dto.getType())
                .breaking(dto.getBreaking() != null ? dto.getBreaking() : false)
                .summary(dto.getSummary())
                .details(dto.getDetails())
                .releasedAt(dto.getReleasedAt())
                .build();
        
        entry = changelogEntryRepository.save(entry);
        return mapper.toChangelogDto(entry);
    }
    
    @Transactional
    public void deleteChangelogEntry(UUID apiId, UUID entryId) {
        ApiContract api = apiContractRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API contract not found with id: " + apiId));
        
        ChangelogEntry entry = changelogEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Changelog entry not found with id: " + entryId));
        
        if (!entry.getApiContract().getId().equals(apiId)) {
            throw new ResourceNotFoundException("Changelog entry does not belong to this API");
        }
        
        changelogEntryRepository.delete(entry);
    }
    
    private Sort buildSort(String sort, String dir) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "updatedAt");
        }
        
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        switch (sort.toLowerCase()) {
            case "name":
                return Sort.by(direction, "name");
            case "updatedat":
                return Sort.by(direction, "updatedAt");
            case "ownerteam":
                return Sort.by(direction, "ownerTeam");
            case "lifecycle":
                return Sort.by(direction, "lifecycle");
            default:
                return Sort.by(Sort.Direction.DESC, "updatedAt");
        }
    }
}

