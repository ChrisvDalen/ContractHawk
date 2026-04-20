package com.chrisvdalen.contracthawk.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "contract_analysis")
public class ContractAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AnalysisStatus status;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "valid_spec")
    private Boolean validSpec;

    @Column(name = "path_count")
    private Integer pathCount;

    @Column(name = "operation_count")
    private Integer operationCount;

    @Column(name = "breaking_changes_detected")
    private Boolean breakingChangesDetected;

    @Column(name = "failure_reason")
    private String failureReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary")
    private Map<String, Object> summary;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ContractAnalysis() {
    }

    public static ContractAnalysis pending(Long contractId, OffsetDateTime createdAt) {
        ContractAnalysis analysis = new ContractAnalysis();
        analysis.contractId = contractId;
        analysis.status = AnalysisStatus.PENDING;
        analysis.createdAt = createdAt;
        return analysis;
    }

    public void markProcessing(OffsetDateTime startedAt) {
        this.status = AnalysisStatus.PROCESSING;
        this.startedAt = startedAt;
    }

    public void markCompleted(OffsetDateTime finishedAt,
                              boolean validSpec,
                              int pathCount,
                              int operationCount,
                              boolean breakingChangesDetected,
                              Map<String, Object> summary) {
        this.status = AnalysisStatus.COMPLETED;
        this.finishedAt = finishedAt;
        this.validSpec = validSpec;
        this.pathCount = pathCount;
        this.operationCount = operationCount;
        this.breakingChangesDetected = breakingChangesDetected;
        this.summary = summary;
    }

    public void markFailed(OffsetDateTime finishedAt, String failureReason) {
        this.status = AnalysisStatus.FAILED;
        this.finishedAt = finishedAt;
        this.failureReason = failureReason;
    }

    public Long getId() { return id; }
    public Long getContractId() { return contractId; }
    public AnalysisStatus getStatus() { return status; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getFinishedAt() { return finishedAt; }
    public Boolean getValidSpec() { return validSpec; }
    public Integer getPathCount() { return pathCount; }
    public Integer getOperationCount() { return operationCount; }
    public Boolean getBreakingChangesDetected() { return breakingChangesDetected; }
    public String getFailureReason() { return failureReason; }
    public Map<String, Object> getSummary() { return summary; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
