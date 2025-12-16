package com.contracthawk.repository;

import com.contracthawk.entity.ApiSyncRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApiSyncRunRepository extends JpaRepository<ApiSyncRun, UUID> {
    
    List<ApiSyncRun> findByApiContractIdOrderByRunAtDesc(UUID apiId);
    
    List<ApiSyncRun> findTop10ByApiContractIdOrderByRunAtDesc(UUID apiId);
}

