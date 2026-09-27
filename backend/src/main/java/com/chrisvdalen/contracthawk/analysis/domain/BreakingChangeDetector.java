package com.chrisvdalen.contracthawk.analysis.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Compares a previous API shape against the current one.
 * <p>
 * Rules (see contracts/specs/breaking-change-detection.md):
 * <ul>
 *   <li>a removed path is a breaking change</li>
 *   <li>a removed HTTP method on an existing path is a breaking change</li>
 *   <li>added paths or methods are not breaking changes</li>
 * </ul>
 * A {@code null} or empty previous shape means "no previous version to compare",
 * in which case nothing is reported as breaking.
 */
public final class BreakingChangeDetector {

    private BreakingChangeDetector() {
    }

    public static BreakingChangeResult detect(Map<String, Set<String>> previous,
                                              Map<String, Set<String>> current) {
        Map<String, Set<String>> prev = normalized(previous);
        Map<String, Set<String>> curr = normalized(current);

        List<BreakingChangeResult.BreakingChange> changes = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : prev.entrySet()) {
            String path = entry.getKey();
            if (!curr.containsKey(path)) {
                changes.add(new BreakingChangeResult.BreakingChange(
                        "PATH_REMOVED", "Path " + path + " was removed"));
                continue;
            }
            for (String method : entry.getValue()) {
                if (!curr.get(path).contains(method)) {
                    changes.add(new BreakingChangeResult.BreakingChange(
                            "METHOD_REMOVED",
                            "Method " + method.toUpperCase(Locale.ROOT) + " on path " + path + " was removed"));
                }
            }
        }

        return BreakingChangeResult.of(changes);
    }

    /**
     * Recovers a path→operations shape from a previously persisted analysis summary.
     * Tolerates the JSON round-trip where nested values come back as {@code List}s.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Set<String>> fromSummary(Map<String, Object> summary) {
        if (summary == null) {
            return Map.of();
        }
        Object raw = summary.get("paths");
        if (!(raw instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }
        Map<String, Set<String>> result = new TreeMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (!(entry.getKey() instanceof String path)) {
                continue;
            }
            Set<String> methods = new TreeSet<>();
            if (entry.getValue() instanceof Iterable<?> iterable) {
                for (Object m : iterable) {
                    if (m instanceof String s) {
                        methods.add(s.toLowerCase(Locale.ROOT));
                    }
                }
            }
            result.put(path, methods);
        }
        return result;
    }

    private static Map<String, Set<String>> normalized(Map<String, Set<String>> shape) {
        Map<String, Set<String>> result = new TreeMap<>();
        if (shape == null) {
            return result;
        }
        for (Map.Entry<String, Set<String>> entry : shape.entrySet()) {
            Set<String> methods = new TreeSet<>();
            if (entry.getValue() != null) {
                for (String m : entry.getValue()) {
                    if (m != null) {
                        methods.add(m.toLowerCase(Locale.ROOT));
                    }
                }
            }
            result.put(entry.getKey(), methods);
        }
        return result;
    }
}
