package com.chrisvdalen.contracthawk.unit;

import com.chrisvdalen.contracthawk.analysis.domain.AnalysisStatus;
import com.chrisvdalen.contracthawk.analysis.domain.ContractAnalysis;
import com.chrisvdalen.contracthawk.analysis.repository.ContractAnalysisRepository;
import com.chrisvdalen.contracthawk.contract.application.ContractResponse;
import com.chrisvdalen.contracthawk.contract.application.ContractUploadService;
import com.chrisvdalen.contracthawk.contract.domain.Contract;
import com.chrisvdalen.contracthawk.contract.repository.ContractRepository;
import com.chrisvdalen.contracthawk.messaging.application.AnalysisJob;
import com.chrisvdalen.contracthawk.messaging.application.AnalysisJobPublisher;
import com.chrisvdalen.contracthawk.shared.exception.BadRequestException;
import com.chrisvdalen.contracthawk.storage.application.FileStorageService;
import com.chrisvdalen.contracthawk.storage.domain.StoredFile;
import com.chrisvdalen.contracthawk.storage.infrastructure.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractUploadServiceTest {

    private ContractRepository contractRepository;
    private ContractAnalysisRepository analysisRepository;
    private FileStorageService fileStorageService;
    private AnalysisJobPublisher analysisJobPublisher;
    private ContractUploadService service;

    private static final StorageProperties PROPERTIES =
            new StorageProperties("/tmp/contracts", List.of("json", "yaml", "yml"));

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        analysisRepository = mock(ContractAnalysisRepository.class);
        fileStorageService = mock(FileStorageService.class);
        analysisJobPublisher = mock(AnalysisJobPublisher.class);
        service = new ContractUploadService(contractRepository, analysisRepository, fileStorageService, analysisJobPublisher, PROPERTIES);
    }

    private static MultipartFile yaml(String name) {
        return new MockMultipartFile("file", name, "application/yaml", "openapi: 3.1.0".getBytes());
    }

    private static void setId(Object entity, long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void uploadRejectsBlankServiceName() throws Exception {
        assertThatThrownBy(() -> service.upload("  ", "1.0.0", yaml("spec.yaml")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("serviceName");
        verify(fileStorageService, never()).store(anyString(), anyString(), anyString(), any());
    }

    @Test
    void uploadRejectsBlankVersion() {
        assertThatThrownBy(() -> service.upload("svc", " ", yaml("spec.yaml")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("version");
    }

    @Test
    void uploadRejectsEmptyFile() {
        MultipartFile empty = new MockMultipartFile("file", "spec.yaml", "application/yaml", new byte[0]);
        assertThatThrownBy(() -> service.upload("svc", "1.0.0", empty))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void uploadRejectsDisallowedExtension() {
        MultipartFile exe = new MockMultipartFile("file", "malware.exe", "application/octet-stream", "x".getBytes());
        assertThatThrownBy(() -> service.upload("svc", "1.0.0", exe))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("files are allowed");
    }

    @Test
    void uploadRejectsMissingExtension() {
        MultipartFile noExt = new MockMultipartFile("file", "README", "text/plain", "x".getBytes());
        assertThatThrownBy(() -> service.upload("svc", "1.0.0", noExt))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("files are allowed");
    }

    @Test
    void uploadMapsStorageTraversalToBadRequest() throws Exception {
        when(fileStorageService.store(anyString(), anyString(), anyString(), any()))
                .thenThrow(new IllegalArgumentException("Storage path escapes base directory: service=.."));

        assertThatThrownBy(() -> service.upload("..", "1.0.0", yaml("spec.yaml")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("path characters");
        verify(contractRepository, never()).save(any());
    }

    @Test
    void uploadWrapsStorageIoFailure() throws Exception {
        when(fileStorageService.store(anyString(), anyString(), anyString(), any()))
                .thenThrow(new IOException("disk full"));

        assertThatThrownBy(() -> service.upload("svc", "1.0.0", yaml("spec.yaml")))
                .isInstanceOf(IllegalStateException.class);
        verify(contractRepository, never()).save(any());
    }

    @Test
    void uploadPersistsContractAndPendingAnalysisThenPublishesJob() throws Exception {
        when(fileStorageService.store(anyString(), anyString(), anyString(), any()))
                .thenReturn(new StoredFile("svc/1.0.0/spec.yaml", 12L));
        doAnswer(inv -> {
            Contract c = inv.getArgument(0);
            setId(c, 7L);
            return c;
        }).when(contractRepository).save(any(Contract.class));
        doAnswer(inv -> {
            ContractAnalysis a = inv.getArgument(0);
            setId(a, 42L);
            return a;
        }).when(analysisRepository).save(any(ContractAnalysis.class));
        when(contractRepository.findTopByServiceNameAndIdNotOrderByUploadedAtDesc("svc", 7L))
                .thenReturn(Optional.empty());

        ContractResponse response = service.upload("svc", "1.0.0", yaml("spec.yaml"));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.serviceName()).isEqualTo("svc");
        assertThat(response.version()).isEqualTo("1.0.0");
        assertThat(response.originalFilename()).isEqualTo("spec.yaml");

        ArgumentCaptor<Contract> contractCaptor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).save(contractCaptor.capture());
        assertThat(contractCaptor.getValue().getStoragePath()).isEqualTo("svc/1.0.0/spec.yaml");

        ArgumentCaptor<ContractAnalysis> analysisCaptor = ArgumentCaptor.forClass(ContractAnalysis.class);
        verify(analysisRepository).save(analysisCaptor.capture());
        assertThat(analysisCaptor.getValue().getStatus()).isEqualTo(AnalysisStatus.PENDING);
        assertThat(analysisCaptor.getValue().getContractId()).isEqualTo(7L);

        ArgumentCaptor<AnalysisJob> jobCaptor = ArgumentCaptor.forClass(AnalysisJob.class);
        verify(analysisJobPublisher).publish(jobCaptor.capture());
        assertThat(jobCaptor.getValue().contractId()).isEqualTo(7L);
        assertThat(jobCaptor.getValue().analysisId()).isEqualTo(42L);
        assertThat(jobCaptor.getValue().storagePath()).isEqualTo("svc/1.0.0/spec.yaml");
        assertThat(jobCaptor.getValue().previousPaths()).isEqualTo(Map.of());
    }

    @Test
    void uploadCarriesPreviousVersionPathsIntoJob() throws Exception {
        when(fileStorageService.store(anyString(), anyString(), anyString(), any()))
                .thenReturn(new StoredFile("svc/2.0.0/spec.yaml", 5L));
        doAnswer(inv -> {
            Contract c = inv.getArgument(0);
            setId(c, 9L);
            return c;
        }).when(contractRepository).save(any(Contract.class));
        doAnswer(inv -> {
            ContractAnalysis a = inv.getArgument(0);
            setId(a, 50L);
            return a;
        }).when(analysisRepository).save(any(ContractAnalysis.class));

        // previous contract of the same service with a completed analysis
        Contract previous = new Contract("svc", "1.0.0", "old.yaml", "svc/1.0.0/old.yaml", OffsetDateTime.now());
        setId(previous, 1L);
        when(contractRepository.findTopByServiceNameAndIdNotOrderByUploadedAtDesc("svc", 9L))
                .thenReturn(Optional.of(previous));

        ContractAnalysis previousAnalysis = ContractAnalysis.pending(1L, OffsetDateTime.now());
        setId(previousAnalysis, 2L);
        previousAnalysis.markCompleted(OffsetDateTime.now(), true, 1, 1, false,
                Map.of("paths", Map.of("/pets", List.of("get", "post"))));
        when(analysisRepository.findTopByContractIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(previousAnalysis));

        service.upload("svc", "2.0.0", yaml("spec.yaml"));

        ArgumentCaptor<AnalysisJob> jobCaptor = ArgumentCaptor.forClass(AnalysisJob.class);
        verify(analysisJobPublisher).publish(jobCaptor.capture());
        assertThat(jobCaptor.getValue().previousPaths())
                .isEqualTo(Map.of("/pets", new java.util.LinkedHashSet<>(List.of("get", "post"))));
    }
}
