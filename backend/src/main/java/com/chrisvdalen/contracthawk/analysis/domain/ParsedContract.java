package com.chrisvdalen.contracthawk.analysis.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ParsedContract(
        boolean valid,
        int pathCount,
        int operationCount,
        List<String> validationMessages,
        Map<String, Set<String>> paths) {

    public ParsedContract {
        paths = paths == null ? Map.of() : paths;
    }
}
