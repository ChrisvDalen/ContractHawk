package com.chrisvdalen.contracthawk.contract.repository;

import com.chrisvdalen.contracthawk.contract.domain.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findTopByServiceNameAndIdNotOrderByUploadedAtDesc(String serviceName, Long excludeId);
}
