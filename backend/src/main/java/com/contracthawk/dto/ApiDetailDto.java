package com.contracthawk.dto;

import com.contracthawk.entity.Lifecycle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiDetailDto {
    private UUID id;
    private String name;
    private String baseUrl;
    private String version;
    private String ownerTeam;
    private Lifecycle lifecycle;
    private String openApiUrl;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private List<EndpointDto> endpoints;
    private List<ChangelogDto> changelog;
}

