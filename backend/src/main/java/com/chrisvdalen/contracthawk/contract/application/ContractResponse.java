package com.chrisvdalen.contracthawk.contract.application;

import com.chrisvdalen.contracthawk.contract.domain.Contract;

import java.time.OffsetDateTime;

public record ContractResponse(
        Long id,
        String serviceName,
        String version,
        String originalFilename,
        OffsetDateTime uploadedAt) {

    public static ContractResponse from(Contract contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getServiceName(),
                contract.getVersion(),
                contract.getOriginalFilename(),
                contract.getUploadedAt());
    }
}
