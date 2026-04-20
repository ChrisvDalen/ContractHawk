package com.chrisvdalen.contracthawk.analysis.application;

import com.chrisvdalen.contracthawk.analysis.domain.AnalysisStatus;
import com.chrisvdalen.contracthawk.analysis.domain.ContractAnalysis;

import java.time.OffsetDateTime;
import java.util.Map;

public record ContractAnalysisResponse(
        Long id,
        Long contractId,
        AnalysisStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        Boolean validSpec,
        Integer pathCount,
        Integer operationCount,
        Boolean breakingChangesDetected,
        String failureReason,
        Map<String, Object> summary) {

    public static ContractAnalysisResponse from(ContractAnalysis analysis) {
        return new ContractAnalysisResponse(
                analysis.getId(),
                analysis.getContractId(),
                analysis.getStatus(),
                analysis.getStartedAt(),
                analysis.getFinishedAt(),
                analysis.getValidSpec(),
                analysis.getPathCount(),
                analysis.getOperationCount(),
                analysis.getBreakingChangesDetected(),
                analysis.getFailureReason(),
                analysis.getSummary());
    }
}
