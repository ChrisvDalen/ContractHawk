package com.contracthawk.repository;

import com.contracthawk.entity.ApiContract;
import com.contracthawk.entity.Lifecycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiContractRepository extends JpaRepository<ApiContract, UUID>, JpaSpecificationExecutor<ApiContract> {
    
    Optional<ApiContract> findByNameAndOwnerTeam(String name, String ownerTeam);
    
    @Query("SELECT DISTINCT a.ownerTeam FROM ApiContract a ORDER BY a.ownerTeam")
    List<String> findAllDistinctOwnerTeams();
}

