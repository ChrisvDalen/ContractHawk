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
public class RefreshSummaryDto {
    private Integer totalApis;
    private Integer succeeded;
    private Integer failed;
    private List<RefreshFailureDto> failures;
}

