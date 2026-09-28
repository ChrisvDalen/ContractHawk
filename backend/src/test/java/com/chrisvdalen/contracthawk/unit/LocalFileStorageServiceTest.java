package com.chrisvdalen.contracthawk.unit;

import com.chrisvdalen.contracthawk.storage.domain.StoredFile;
import com.chrisvdalen.contracthawk.storage.infrastructure.LocalFileStorageService;
import com.chrisvdalen.contracthawk.storage.infrastructure.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalFileStorageService(new StorageProperties(tempDir.toString(), List.of("json", "yaml", "yml")));
    }

    @Test
    void storeWritesFileUnderBaseDirAndReturnsRelativePath() throws IOException {
        byte[] payload = "{\"openapi\":\"3.1.0\"}".getBytes();
        StoredFile stored = service.store("svc", "1.0.0", "spec.yaml", new java.io.ByteArrayInputStream(payload));

        Path written = tempDir.resolve(stored.storagePath());
        assertThat(written).exists();
        assertThat(Files.readAllBytes(written)).isEqualTo(payload);
        assertThat(stored.sizeBytes()).isEqualTo(payload.length);
        assertThat(stored.storagePath()).doesNotStartWith("..");
        // stored under the sanitized service/version dirs
        assertThat(written.startsWith(tempDir.resolve("svc").resolve("1.0.0"))).isTrue();
    }

    @Test
    void storeSanitizesPathSeparatorsInServiceAndVersion() throws IOException {
        byte[] payload = "{}".getBytes();
        StoredFile stored = service.store("svc/sub", "1.0/2.0", "spec.yaml", new java.io.ByteArrayInputStream(payload));

        Path written = tempDir.resolve(stored.storagePath());
        assertThat(written).exists();
        // '/' and '\\' are replaced, so no nested dirs are created beyond svc/sub
        assertThat(written.startsWith(tempDir.resolve("svc_sub"))).isTrue();
    }

    @Test
    void storeRejectsParentTraversalInServiceName() throws IOException {
        byte[] payload = "{}".getBytes();
        assertThatThrownBy(() -> service.store("..", "1.0.0", "spec.yaml", new java.io.ByteArrayInputStream(payload)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("escape");
    }

    @Test
    void storeContainsResultInsideBaseDirEvenWhenVersionIsParent() throws IOException {
        byte[] payload = "{}".getBytes();
        StoredFile stored = service.store("svc", "..", "spec.yaml", new java.io.ByteArrayInputStream(payload));
        // ".." cancels the service dir, so the file must still land inside baseDir
        Path written = tempDir.resolve(stored.storagePath()).normalize();
        assertThat(written.toAbsolutePath().startsWith(tempDir.toAbsolutePath().normalize())).isTrue();
    }

    @Test
    void storeRejectsDoubleParentTraversal() throws IOException {
        byte[] payload = "{}".getBytes();
        assertThatThrownBy(() -> service.store("..", "..", "spec.yaml", new java.io.ByteArrayInputStream(payload)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("escape");
    }

    @Test
    void readReturnsContentForStoredFile() throws IOException {
        byte[] payload = "hello".getBytes();
        StoredFile stored = service.store("svc", "1.0.0", "spec.yaml", new java.io.ByteArrayInputStream(payload));
        try (InputStream in = service.read(stored.storagePath())) {
            assertThat(in.readAllBytes()).isEqualTo(payload);
        }
    }

    @Test
    void readRejectsPathThatEscapesBaseDir() throws IOException {
        assertThatThrownBy(() -> service.read("../secret.yaml"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("escape");
        assertThatThrownBy(() -> service.read("../../etc/passwd"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("escape");
    }
}
