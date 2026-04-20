package com.chrisvdalen.contracthawk.storage.application;

import com.chrisvdalen.contracthawk.storage.domain.StoredFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {

    StoredFile store(String serviceName, String version, String originalFilename, InputStream content) throws IOException;

    InputStream read(String storagePath) throws IOException;
}
