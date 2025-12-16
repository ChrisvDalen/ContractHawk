package com.contracthawk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDto {
    private UUID syncRunId;
    private Integer addedCount;
    private Integer updatedCount;
    private Integer deletedCount;
    private Boolean breaksDetected;
    private List<BreakingChangeDto> breakingChanges;
}

