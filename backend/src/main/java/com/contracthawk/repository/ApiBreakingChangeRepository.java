package com.contracthawk.repository;

import com.contracthawk.entity.ApiBreakingChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApiBreakingChangeRepository extends JpaRepository<ApiBreakingChange, UUID> {
    
    List<ApiBreakingChange> findBySyncRunId(UUID syncRunId);
}

