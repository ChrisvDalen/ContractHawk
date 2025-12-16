package com.contracthawk.repository;

import com.contracthawk.entity.ChangelogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChangelogEntryRepository extends JpaRepository<ChangelogEntry, UUID> {
    
    List<ChangelogEntry> findByApiContractIdOrderByReleasedAtDesc(UUID apiId);
}

