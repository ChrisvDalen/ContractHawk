package com.chrisvdalen.contracthawk.analysis.repository;

import com.chrisvdalen.contracthawk.analysis.domain.ContractAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractAnalysisRepository extends JpaRepository<ContractAnalysis, Long> {

    List<ContractAnalysis> findByContractIdOrderByCreatedAtDesc(Long contractId);

    Optional<ContractAnalysis> findTopByContractIdOrderByCreatedAtDesc(Long contractId);
}
