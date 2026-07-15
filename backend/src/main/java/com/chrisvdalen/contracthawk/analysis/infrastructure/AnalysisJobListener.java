package com.chrisvdalen.contracthawk.analysis.infrastructure;

import com.chrisvdalen.contracthawk.analysis.application.ContractAnalysisService;
import com.chrisvdalen.contracthawk.messaging.application.AnalysisJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobListener {

    private static final Logger log = LoggerFactory.getLogger(AnalysisJobListener.class);

    private final ContractAnalysisService analysisService;

    public AnalysisJobListener(ContractAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @RabbitListener(queues = "${contracthawk.messaging.queue}")
    public void handle(AnalysisJob job) {
        log.debug("Received analysis job contractId={} analysisId={}", job.contractId(), job.analysisId());
        analysisService.process(job);
    }
}
