package com.chrisvdalen.contracthawk.storage.infrastructure;

import com.chrisvdalen.contracthawk.storage.application.FileStorageService;
import com.chrisvdalen.contracthawk.storage.domain.StoredFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path baseDir;

    public LocalFileStorageService(StorageProperties properties) {
        this.baseDir = Path.of(properties.localDir()).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(String serviceName, String version, String originalFilename, InputStream content) throws IOException {
        String safeService = sanitize(serviceName);
        String safeVersion = sanitize(version);
        String extension = extensionOf(originalFilename);
        String filename = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        Path targetDir = baseDir.resolve(safeService).resolve(safeVersion);
        Files.createDirectories(targetDir);

        Path target = targetDir.resolve(filename);
        long size = Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);

        return new StoredFile(baseDir.relativize(target).toString(), size);
    }

    @Override
    public InputStream read(String storagePath) throws IOException {
        Path resolved = baseDir.resolve(storagePath).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new IOException("Storage path escapes base directory: " + storagePath);
        }
        return Files.newInputStream(resolved);
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase();
    }
}
