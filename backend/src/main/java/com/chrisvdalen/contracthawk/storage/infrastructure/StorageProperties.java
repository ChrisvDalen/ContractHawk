package com.chrisvdalen.contracthawk.storage.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "contracthawk.storage")
public record StorageProperties(String localDir, List<String> allowedExtensions) {
}
