package com.chrisvdalen.contracthawk.analysis.domain;

import java.util.List;

public record ParsedContract(boolean valid, int pathCount, int operationCount, List<String> validationMessages) {
}
