package com.chrisvdalen.contracthawk.contract.application;

import com.chrisvdalen.contracthawk.analysis.domain.ContractAnalysis;
import com.chrisvdalen.contracthawk.analysis.repository.ContractAnalysisRepository;
import com.chrisvdalen.contracthawk.contract.domain.Contract;
import com.chrisvdalen.contracthawk.contract.repository.ContractRepository;
import com.chrisvdalen.contracthawk.messaging.application.AnalysisJob;
import com.chrisvdalen.contracthawk.messaging.application.AnalysisJobPublisher;
import com.chrisvdalen.contracthawk.shared.exception.BadRequestException;
import com.chrisvdalen.contracthawk.storage.application.FileStorageService;
import com.chrisvdalen.contracthawk.storage.domain.StoredFile;
import com.chrisvdalen.contracthawk.storage.infrastructure.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContractUploadService {

    private static final Logger log = LoggerFactory.getLogger(ContractUploadService.class);

    private final ContractRepository contractRepository;
    private final ContractAnalysisRepository analysisRepository;
    private final FileStorageService fileStorageService;
    private final AnalysisJobPublisher analysisJobPublisher;
    private final Set<String> allowedExtensions;

    public ContractUploadService(ContractRepository contractRepository,
                                 ContractAnalysisRepository analysisRepository,
                                 FileStorageService fileStorageService,
                                 AnalysisJobPublisher analysisJobPublisher,
                                 StorageProperties storageProperties) {
        this.contractRepository = contractRepository;
        this.analysisRepository = analysisRepository;
        this.fileStorageService = fileStorageService;
        this.analysisJobPublisher = analysisJobPublisher;
        this.allowedExtensions = storageProperties.allowedExtensions().stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Transactional
    public ContractResponse upload(String serviceName, String version, MultipartFile file) {
        validate(serviceName, version, file);

        StoredFile stored;
        try {
            stored = fileStorageService.store(serviceName, version, file.getOriginalFilename(), file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded contract", e);
        }

        OffsetDateTime now = OffsetDateTime.now();
        Contract contract = contractRepository.save(
                new Contract(serviceName, version, file.getOriginalFilename(), stored.storagePath(), now));

        ContractAnalysis analysis = analysisRepository.save(ContractAnalysis.pending(contract.getId(), now));

        analysisJobPublisher.publish(new AnalysisJob(contract.getId(), analysis.getId()));

        log.info("Uploaded contract id={} service={} version={}", contract.getId(), serviceName, version);
        return ContractResponse.from(contract);
    }

    private void validate(String serviceName, String version, MultipartFile file) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new BadRequestException("INVALID_SERVICE_NAME", "serviceName must not be blank");
        }
        if (version == null || version.isBlank()) {
            throw new BadRequestException("INVALID_VERSION", "version must not be blank");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("EMPTY_FILE", "Uploaded file must not be empty");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (extension.isEmpty() || !allowedExtensions.contains(extension)) {
            throw new BadRequestException("UNSUPPORTED_FILE_EXTENSION",
                    "Only " + allowedExtensions + " files are allowed");
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
