package com.chrisvdalen.contracthawk.unit;

import com.chrisvdalen.contracthawk.analysis.domain.BreakingChangeDetector;
import com.chrisvdalen.contracthawk.analysis.domain.BreakingChangeResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class BreakingChangeDetectorTest {

    private static Map<String, Set<String>> shape(Object... pathAndMethodPairs) {
        Map<String, Set<String>> result = new TreeMap<>();
        for (int i = 0; i < pathAndMethodPairs.length; i += 2) {
            String path = (String) pathAndMethodPairs[i];
            String method = (String) pathAndMethodPairs[i + 1];
            result.computeIfAbsent(path, p -> new TreeSet<>()).add(method.toLowerCase());
        }
        return result;
    }

    @Test
    void noPreviousVersionIsNotBreaking() {
        BreakingChangeResult result = BreakingChangeDetector.detect(Map.of(), shape("/orders", "get"));
        assertThat(result.detected()).isFalse();
        assertThat(result.changes()).isEmpty();
    }

    @Test
    void nullPreviousIsTreatedAsNoPreviousVersion() {
        assertThat(BreakingChangeDetector.detect(null, shape("/orders", "get")).detected()).isFalse();
        assertThat(BreakingChangeDetector.detect(null, null).detected()).isFalse();
    }

    @Test
    void identicalShapesAreNotBreaking() {
        Map<String, Set<String>> shape = shape("/orders", "get", "/orders", "post", "/orders/{id}", "get");
        BreakingChangeResult result = BreakingChangeDetector.detect(shape, shape);
        assertThat(result.detected()).isFalse();
    }

    @Test
    void removedPathIsBreaking() {
        BreakingChangeResult result = BreakingChangeDetector.detect(
                shape("/orders", "get", "/users", "get"),
                shape("/orders", "get"));
        assertThat(result.detected()).isTrue();
        assertThat(result.changes()).satisfiesExactly(change -> {
            assertThat(change.code()).isEqualTo("PATH_REMOVED");
            assertThat(change.description()).isEqualTo("Path /users was removed");
        });
    }

    @Test
    void removedMethodOnExistingPathIsBreaking() {
        BreakingChangeResult result = BreakingChangeDetector.detect(
                shape("/orders", "get", "/orders", "post"),
                shape("/orders", "get"));
        assertThat(result.detected()).isTrue();
        assertThat(result.changes()).satisfiesExactly(change -> {
            assertThat(change.code()).isEqualTo("METHOD_REMOVED");
            assertThat(change.description()).isEqualTo("Method POST on path /orders was removed");
        });
    }

    @Test
    void addedPathIsNotBreaking() {
        BreakingChangeResult result = BreakingChangeDetector.detect(
                shape("/orders", "get"),
                shape("/orders", "get", "/users", "get"));
        assertThat(result.detected()).isFalse();
    }

    @Test
    void addedMethodIsNotBreaking() {
        BreakingChangeResult result = BreakingChangeDetector.detect(
                shape("/orders", "get"),
                shape("/orders", "get", "/orders", "post"));
        assertThat(result.detected()).isFalse();
    }

    @Test
    void addedPathWithRemovalIsBreakingForTheRemovalOnly() {
        BreakingChangeResult result = BreakingChangeDetector.detect(
                shape("/orders", "get", "/users", "post"),
                shape("/orders", "get", "/new", "post"));
        assertThat(result.detected()).isTrue();
        assertThat(result.changes())
                .extracting(BreakingChangeResult.BreakingChange::code)
                .containsExactly("PATH_REMOVED");
    }

    @Test
    void methodMatchingIsCaseInsensitive() {
        BreakingChangeResult same = BreakingChangeDetector.detect(shape("/orders", "GET"), shape("/orders", "get"));
        assertThat(same.detected()).isFalse();

        BreakingChangeResult removed = BreakingChangeDetector.detect(shape("/orders", "GET"), Map.of());
        assertThat(removed.detected()).isTrue();
        assertThat(removed.changes()).first().satisfies(c ->
                assertThat(c.code()).isEqualTo("PATH_REMOVED"));
    }

    @Test
    void fromSummaryReconstructsShapeFromJsonLikeMap() {
        // JSON round-trip: nested values arrive as Lists, not Sets
        Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("pathCount", 2);
        summary.put("paths", Map.of(
                "/orders", List.of("get", "post"),
                "/orders/{id}", List.of("get")));

        Map<String, Set<String>> shape = BreakingChangeDetector.fromSummary(summary);

        assertThat(shape).hasSize(2);
        assertThat(shape.get("/orders")).containsExactlyInAnyOrder("get", "post");
        assertThat(shape.get("/orders/{id}")).containsExactly("get");
    }

    @Test
    void fromSummaryToleratesMissingOrMalformedData() {
        assertThat(BreakingChangeDetector.fromSummary(null)).isEmpty();
        assertThat(BreakingChangeDetector.fromSummary(Map.of())).isEmpty();
        Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("paths", "not-a-map");
        assertThat(BreakingChangeDetector.fromSummary(summary)).isEmpty();
    }
}
