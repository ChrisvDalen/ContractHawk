package com.chrisvdalen.contracthawk.contract.application;

import com.chrisvdalen.contracthawk.analysis.domain.AnalysisStatus;

import java.time.OffsetDateTime;

public record ContractListItem(
        Long id,
        String serviceName,
        String version,
        String originalFilename,
        OffsetDateTime uploadedAt,
        AnalysisStatus latestStatus,
        Boolean breakingChangesDetected) {
}
