package com.contracthawk.scheduler;

import com.contracthawk.entity.SyncMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "contracthawk.open-api-refresh")
@Data
public class OpenApiRefreshProperties {
    
    private boolean enabled = false;
    private String cron = "0 0 3 * * *"; // Default: 3 AM daily
    private SyncMode mode = SyncMode.MERGE;
}

