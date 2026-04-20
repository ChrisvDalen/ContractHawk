package com.chrisvdalen.contracthawk.contract.application;

import com.chrisvdalen.contracthawk.contract.domain.Contract;

import java.time.OffsetDateTime;

public record ContractDetailsResponse(
        Long id,
        String serviceName,
        String version,
        String originalFilename,
        String storagePath,
        OffsetDateTime uploadedAt) {

    public static ContractDetailsResponse from(Contract contract) {
        return new ContractDetailsResponse(
                contract.getId(),
                contract.getServiceName(),
                contract.getVersion(),
                contract.getOriginalFilename(),
                contract.getStoragePath(),
                contract.getUploadedAt());
    }
}
