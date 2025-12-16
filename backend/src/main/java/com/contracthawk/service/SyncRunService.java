package com.contracthawk.service;

import com.contracthawk.dto.ApiSyncRunDto;
import com.contracthawk.entity.ApiSyncRun;
import com.contracthawk.exception.ResourceNotFoundException;
import com.contracthawk.mapper.SyncRunMapper;
import com.contracthawk.repository.ApiBreakingChangeRepository;
import com.contracthawk.repository.ApiSyncRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyncRunService {
    
    private final ApiSyncRunRepository apiSyncRunRepository;
    private final ApiBreakingChangeRepository apiBreakingChangeRepository;
    private final SyncRunMapper mapper;
    
    @Transactional(readOnly = true)
    public List<ApiSyncRunDto> getSyncRunsForApi(UUID apiId) {
        List<ApiSyncRun> runs = apiSyncRunRepository.findByApiContractIdOrderByRunAtDesc(apiId);
        
        return runs.stream()
                .map(run -> {
                    List<com.contracthawk.entity.ApiBreakingChange> breakingChanges = 
                            apiBreakingChangeRepository.findBySyncRunId(run.getId());
                    return mapper.toDto(run, breakingChanges);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ApiSyncRunDto getSyncRun(UUID syncRunId) {
        ApiSyncRun run = apiSyncRunRepository.findById(syncRunId)
                .orElseThrow(() -> new ResourceNotFoundException("Sync run not found with id: " + syncRunId));
        
        List<com.contracthawk.entity.ApiBreakingChange> breakingChanges = 
                apiBreakingChangeRepository.findBySyncRunId(syncRunId);
        
        return mapper.toDto(run, breakingChanges);
    }
}

