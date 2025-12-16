package com.contracthawk.mapper;

import com.contracthawk.dto.*;
import com.contracthawk.entity.ApiContract;
import com.contracthawk.entity.ChangelogEntry;
import com.contracthawk.entity.Endpoint;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApiContractMapper {
    
    public ApiContract toEntity(ApiCreateDto dto) {
        return ApiContract.builder()
                .name(dto.getName())
                .baseUrl(dto.getBaseUrl())
                .version(dto.getVersion())
                .ownerTeam(dto.getOwnerTeam())
                .lifecycle(dto.getLifecycle())
                .openApiUrl(dto.getOpenApiUrl())
                .description(dto.getDescription())
                .build();
    }
    
    public void updateEntity(ApiContract entity, ApiUpdateDto dto) {
        entity.setName(dto.getName());
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setVersion(dto.getVersion());
        entity.setOwnerTeam(dto.getOwnerTeam());
        entity.setLifecycle(dto.getLifecycle());
        entity.setOpenApiUrl(dto.getOpenApiUrl());
        entity.setDescription(dto.getDescription());
    }
    
    public ApiSummaryDto toSummaryDto(ApiContract entity) {
        return ApiSummaryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .baseUrl(entity.getBaseUrl())
                .version(entity.getVersion())
                .ownerTeam(entity.getOwnerTeam())
                .lifecycle(entity.getLifecycle())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public ApiDetailDto toDetailDto(ApiContract entity) {
        List<EndpointDto> endpoints = entity.getEndpoints().stream()
                .map(this::toEndpointDto)
                .collect(Collectors.toList());
        
        List<ChangelogDto> changelog = entity.getChangelogEntries().stream()
                .sorted(Comparator.comparing(ChangelogEntry::getReleasedAt).reversed())
                .map(this::toChangelogDto)
                .collect(Collectors.toList());
        
        return ApiDetailDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .baseUrl(entity.getBaseUrl())
                .version(entity.getVersion())
                .ownerTeam(entity.getOwnerTeam())
                .lifecycle(entity.getLifecycle())
                .openApiUrl(entity.getOpenApiUrl())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .endpoints(endpoints)
                .changelog(changelog)
                .build();
    }
    
    public EndpointDto toEndpointDto(Endpoint entity) {
        return EndpointDto.builder()
                .id(entity.getId())
                .method(entity.getMethod())
                .path(entity.getPath())
                .description(entity.getDescription())
                .deprecated(entity.getDeprecated())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    
    public ChangelogDto toChangelogDto(ChangelogEntry entity) {
        return ChangelogDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .breaking(entity.getBreaking())
                .summary(entity.getSummary())
                .details(entity.getDetails())
                .releasedAt(entity.getReleasedAt())
                .build();
    }
}

