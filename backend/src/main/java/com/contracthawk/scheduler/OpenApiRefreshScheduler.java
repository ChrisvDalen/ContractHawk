package com.contracthawk.scheduler;

import com.contracthawk.entity.SyncMode;
import com.contracthawk.service.OpenApiSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "contracthawk.open-api-refresh.enabled", havingValue = "true", matchIfMissing = false)
public class OpenApiRefreshScheduler {
    
    private final OpenApiSyncService openApiSyncService;
    private final OpenApiRefreshProperties properties;
    
    @Scheduled(cron = "${contracthawk.open-api-refresh.cron:0 0 3 * * *}")
    public void refreshAllApis() {
        log.info("Starting scheduled OpenAPI refresh (mode: {})", properties.getMode());
        
        try {
            var summary = openApiSyncService.refreshAll(properties.getMode());
            log.info("Scheduled OpenAPI refresh completed: {} succeeded, {} failed out of {} total APIs",
                    summary.getSucceeded(), summary.getFailed(), summary.getTotalApis());
            
            if (!summary.getFailures().isEmpty()) {
                summary.getFailures().forEach(failure ->
                        log.warn("Failed to refresh API {} ({}): {}", 
                                failure.getApiId(), failure.getApiName(), failure.getReason()));
            }
        } catch (Exception e) {
            log.error("Error during scheduled OpenAPI refresh", e);
        }
    }
}

