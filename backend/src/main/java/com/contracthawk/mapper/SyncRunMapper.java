package com.contracthawk.mapper;

import com.contracthawk.dto.ApiSyncRunDto;
import com.contracthawk.dto.BreakingChangeDto;
import com.contracthawk.entity.ApiBreakingChange;
import com.contracthawk.entity.ApiSyncRun;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SyncRunMapper {
    
    public ApiSyncRunDto toDto(ApiSyncRun entity, List<ApiBreakingChange> breakingChanges) {
        List<BreakingChangeDto> breakingChangeDtos = breakingChanges.stream()
                .map(this::toBreakingChangeDto)
                .collect(Collectors.toList());
        
        return ApiSyncRunDto.builder()
                .id(entity.getId())
                .apiId(entity.getApiContract().getId())
                .runAt(entity.getRunAt())
                .status(entity.getStatus())
                .mode(entity.getMode())
                .addedCount(entity.getAddedCount())
                .updatedCount(entity.getUpdatedCount())
                .deletedCount(entity.getDeletedCount())
                .breaksDetected(entity.getBreaksDetected())
                .errorMessage(entity.getErrorMessage())
                .breakingChanges(breakingChangeDtos)
                .build();
    }
    
    private BreakingChangeDto toBreakingChangeDto(ApiBreakingChange entity) {
        return BreakingChangeDto.builder()
                .type(entity.getType())
                .method(entity.getMethod())
                .path(entity.getPath())
                .details(entity.getDetails())
                .build();
    }
}

