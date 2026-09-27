package com.chrisvdalen.contracthawk.analysis.application;

import com.chrisvdalen.contracthawk.analysis.domain.BreakingChangeDetector;
import com.chrisvdalen.contracthawk.analysis.domain.BreakingChangeResult;
import com.chrisvdalen.contracthawk.analysis.domain.ContractAnalysis;
import com.chrisvdalen.contracthawk.analysis.domain.ParsedContract;
import com.chrisvdalen.contracthawk.analysis.repository.ContractAnalysisRepository;
import com.chrisvdalen.contracthawk.messaging.application.AnalysisJob;
import com.chrisvdalen.contracthawk.storage.application.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ContractAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ContractAnalysisService.class);

    private final ContractAnalysisRepository analysisRepository;
    private final FileStorageService fileStorageService;
    private final ContractParser contractParser;

    public ContractAnalysisService(ContractAnalysisRepository analysisRepository,
                                   FileStorageService fileStorageService,
                                   ContractParser contractParser) {
        this.analysisRepository = analysisRepository;
        this.fileStorageService = fileStorageService;
        this.contractParser = contractParser;
    }

    @Transactional
    public void process(AnalysisJob job) {
        ContractAnalysis analysis = analysisRepository.findById(job.analysisId())
                .orElseThrow(() -> new IllegalStateException("Analysis " + job.analysisId() + " not found"));

        analysis.markProcessing(OffsetDateTime.now());
        analysisRepository.save(analysis);

        ParsedContract parsed;
        try (InputStream content = fileStorageService.read(job.storagePath())) {
            parsed = contractParser.parse(content);
        } catch (IOException e) {
            throw new ContractProcessingException("Failed to read contract at " + job.storagePath(), e);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("pathCount", parsed.pathCount());
        summary.put("operationCount", parsed.operationCount());
        summary.put("validSpec", parsed.valid());
        if (!parsed.validationMessages().isEmpty()) {
            summary.put("validationMessages", parsed.validationMessages());
        }
        summary.put("paths", parsed.paths());

        BreakingChangeResult breaking = BreakingChangeDetector.detect(
                job.previousPaths() == null ? Map.of() : job.previousPaths(), parsed.paths());
        if (breaking.detected()) {
            summary.put("breakingChanges", breaking.changes());
        }

        analysis.markCompleted(
                OffsetDateTime.now(),
                parsed.valid(),
                parsed.pathCount(),
                parsed.operationCount(),
                breaking.detected(),
                summary);
        analysisRepository.save(analysis);

        log.info("Analysis {} completed for contract {} (paths={}, operations={}, valid={}, breakingChanges={})",
                analysis.getId(), job.contractId(), parsed.pathCount(), parsed.operationCount(),
                parsed.valid(), breaking.detected());
    }

    @Transactional
    public void markFailed(AnalysisJob job, String reason) {
        analysisRepository.findById(job.analysisId()).ifPresent(analysis -> {
            analysis.markFailed(OffsetDateTime.now(), reason);
            analysisRepository.save(analysis);
            log.warn("Analysis {} marked FAILED for contract {}: {}", analysis.getId(), job.contractId(), reason);
        });
    }
}
