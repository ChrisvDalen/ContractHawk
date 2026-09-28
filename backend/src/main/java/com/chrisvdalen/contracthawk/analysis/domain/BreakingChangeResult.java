package com.chrisvdalen.contracthawk.analysis.domain;

import java.util.List;

public record BreakingChangeResult(boolean detected, List<BreakingChange> changes) {

    public record BreakingChange(String code, String description) {
    }

    public static BreakingChangeResult none() {
        return new BreakingChangeResult(false, List.of());
    }

    public static BreakingChangeResult of(List<BreakingChange> changes) {
        return new BreakingChangeResult(!changes.isEmpty(), List.copyOf(changes));
    }
}
