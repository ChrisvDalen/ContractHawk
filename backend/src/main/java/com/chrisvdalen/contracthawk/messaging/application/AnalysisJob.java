package com.chrisvdalen.contracthawk.messaging.application;

import java.util.Map;
import java.util.Set;

public record AnalysisJob(Long contractId, Long analysisId, String storagePath,
                          Map<String, Set<String>> previousPaths) {
}
