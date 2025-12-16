package com.contracthawk.dto;

import com.contracthawk.entity.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangedEndpointDto {
    private EndpointDto current;
    private EndpointDto proposed;
    private String changeDescription;
}

