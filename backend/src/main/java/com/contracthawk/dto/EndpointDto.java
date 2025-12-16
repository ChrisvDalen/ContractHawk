package com.contracthawk.dto;

import com.contracthawk.entity.HttpMethod;
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
public class EndpointDto {
    private UUID id;
    private HttpMethod method;
    private String path;
    private String description;
    private Boolean deprecated;
    private Instant createdAt;
}

