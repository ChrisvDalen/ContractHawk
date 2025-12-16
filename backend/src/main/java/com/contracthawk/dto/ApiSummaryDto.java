package com.contracthawk.dto;

import com.contracthawk.entity.Lifecycle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSummaryDto {
    private UUID id;
    private String name;
    private String baseUrl;
    private String version;
    private String ownerTeam;
    private Lifecycle lifecycle;
    private Instant updatedAt;
}

