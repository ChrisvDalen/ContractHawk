package com.chrisvdalen.contracthawk.unit;

import com.chrisvdalen.contracthawk.analysis.application.ContractAnalysisService;
import com.chrisvdalen.contracthawk.analysis.application.ContractParser;
import com.chrisvdalen.contracthawk.analysis.application.ContractProcessingException;
import com.chrisvdalen.contracthawk.analysis.domain.AnalysisStatus;
import com.chrisvdalen.contracthawk.analysis.domain.BreakingChangeResult;
import com.chrisvdalen.contracthawk.analysis.domain.ContractAnalysis;
import com.chrisvdalen.contracthawk.analysis.domain.ParsedContract;
import com.chrisvdalen.contracthawk.analysis.repository.ContractAnalysisRepository;
import com.chrisvdalen.contracthawk.messaging.application.AnalysisJob;
import com.chrisvdalen.contracthawk.storage.application.FileStorageService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContractAnalysisServiceTest {

    private ContractAnalysisRepository analysisRepository;
    private FileStorageService fileStorageService;
    private ContractParser contractParser;
    private ContractAnalysisService service;

    @BeforeEach
    void setUp() {
        analysisRepository = mock(ContractAnalysisRepository.class);
        fileStorageService = mock(FileStorageService.class);
        contractParser = mock(ContractParser.class);
        service = new ContractAnalysisService(analysisRepository, fileStorageService, contractParser, new SimpleMeterRegistry());
    }

    @Test
    void processMarksAnalysisProcessingThenCompletedWithCounts() throws Exception {
        ContractAnalysis analysis = ContractAnalysis.pending(10L, OffsetDateTime.now());
        setId(analysis, 20L);

        List<AnalysisStatus> savedStatuses = new ArrayList<>();
        doAnswer(inv -> {
            ContractAnalysis ca = inv.getArgument(0);
            savedStatuses.add(ca.getStatus());
            return ca;
        }).when(analysisRepository).save(any());

        when(analysisRepository.findById(20L)).thenReturn(Optional.of(analysis));
        when(fileStorageService.read("order-service/1.0.0/spec.yaml"))
                .thenReturn(new ByteArrayInputStream("ignored".getBytes()));
        when(contractParser.parse(any())).thenReturn(
                new ParsedContract(true, 2, 3, List.of(), Map.of("/orders", Set.of("get", "post"))));

        service.process(new AnalysisJob(10L, 20L, "order-service/1.0.0/spec.yaml", Map.of()));

        assertThat(savedStatuses).containsExactly(AnalysisStatus.PROCESSING, AnalysisStatus.COMPLETED);
        assertThat(analysis.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(analysis.getPathCount()).isEqualTo(2);
        assertThat(analysis.getOperationCount()).isEqualTo(3);
        assertThat(analysis.getValidSpec()).isTrue();
        assertThat(analysis.getBreakingChangesDetected()).isFalse();
        assertThat(analysis.getSummary())
                .containsEntry("pathCount", 2)
                .containsEntry("operationCount", 3)
                .containsEntry("validSpec", true)
                .containsEntry("paths", Map.of("/orders", Set.of("get", "post")));
        assertThat(analysis.getStartedAt()).isNotNull();
        assertThat(analysis.getFinishedAt()).isNotNull();
    }

    @Test
    void processFlagsBreakingChangeWhenPreviousPathWasRemoved() throws Exception {
        ContractAnalysis analysis = ContractAnalysis.pending(10L, OffsetDateTime.now());
        setId(analysis, 20L);

        when(analysisRepository.findById(20L)).thenReturn(Optional.of(analysis));
        when(fileStorageService.read("path")).thenReturn(new ByteArrayInputStream("ignored".getBytes()));
        when(contractParser.parse(any())).thenReturn(
                new ParsedContract(true, 1, 1, List.of(), Map.of("/orders", Set.of("get"))));

        service.process(new AnalysisJob(10L, 20L, "path",
                Map.of("/orders", Set.of("get", "post"), "/users", Set.of("get"))));

        assertThat(analysis.getBreakingChangesDetected()).isTrue();
        assertThat(analysis.getSummary())
                .containsEntry("breakingChanges", List.of(
                        new BreakingChangeResult.BreakingChange("METHOD_REMOVED", "Method POST on path /orders was removed"),
                        new BreakingChangeResult.BreakingChange("PATH_REMOVED", "Path /users was removed")));
    }

    @Test
    void processThrowsWhenFileCannotBeRead() throws Exception {
        ContractAnalysis analysis = ContractAnalysis.pending(1L, OffsetDateTime.now());
        setId(analysis, 2L);

        when(analysisRepository.findById(2L)).thenReturn(Optional.of(analysis));
        when(fileStorageService.read(any())).thenThrow(new IOException("disk error"));

        assertThatThrownBy(() -> service.process(new AnalysisJob(1L, 2L, "path", Map.of())))
                .isInstanceOf(ContractProcessingException.class);
    }

    @Test
    void markFailedTransitionsAnalysisToFailed() {
        ContractAnalysis analysis = ContractAnalysis.pending(1L, OffsetDateTime.now());
        setId(analysis, 2L);
        when(analysisRepository.findById(2L)).thenReturn(Optional.of(analysis));

        List<AnalysisStatus> saved = new ArrayList<>();
        doAnswer(inv -> {
            saved.add(((ContractAnalysis) inv.getArgument(0)).getStatus());
            return inv.getArgument(0);
        }).when(analysisRepository).save(any());

        service.markFailed(new AnalysisJob(1L, 2L, "path", Map.of()), "retries exhausted");

        assertThat(saved).containsExactly(AnalysisStatus.FAILED);
        assertThat(analysis.getFailureReason()).isEqualTo("retries exhausted");
    }

    private static void setId(Object entity, Long id) {
        try {
            Field idField = findField(entity.getClass(), "id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
