package com.contracthawk.repository;

import com.contracthawk.entity.Endpoint;
import com.contracthawk.entity.HttpMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {
    
    Optional<Endpoint> findByApiContractIdAndMethodAndPath(UUID apiId, HttpMethod method, String path);
    
    boolean existsByApiContractIdAndMethodAndPath(UUID apiId, HttpMethod method, String path);
    
    List<Endpoint> findByApiContractId(UUID apiId);
}
