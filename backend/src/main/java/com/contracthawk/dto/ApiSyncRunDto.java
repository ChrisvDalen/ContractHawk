package com.contracthawk.dto;

import com.contracthawk.entity.SyncMode;
import com.contracthawk.entity.SyncStatus;
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
public class ApiSyncRunDto {
    private UUID id;
    private UUID apiId;
    private Instant runAt;
    private SyncStatus status;
    private SyncMode mode;
    private Integer addedCount;
    private Integer updatedCount;
    private Integer deletedCount;
    private Boolean breaksDetected;
    private String errorMessage;
    private List<BreakingChangeDto> breakingChanges;
}

