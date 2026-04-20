package com.chrisvdalen.contracthawk.analysis.infrastructure;

import com.chrisvdalen.contracthawk.analysis.application.ContractAnalysisService;
import com.chrisvdalen.contracthawk.messaging.application.AnalysisJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisDeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(AnalysisDeadLetterListener.class);

    private final ContractAnalysisService analysisService;

    public AnalysisDeadLetterListener(ContractAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @RabbitListener(queues = "${contracthawk.messaging.dead-letter-queue}")
    public void handle(AnalysisJob job) {
        log.warn("Analysis job reached dead-letter queue contractId={} analysisId={}", job.contractId(), job.analysisId());
        analysisService.markFailed(job, "Analysis failed after all retry attempts were exhausted");
    }
}
