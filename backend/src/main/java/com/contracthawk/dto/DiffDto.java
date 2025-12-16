package com.contracthawk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffDto {
    private List<EndpointDto> addedEndpoints;
    private List<EndpointDto> removedEndpoints;
    private List<ChangedEndpointDto> changedEndpoints;
}

