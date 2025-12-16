package com.contracthawk.controller;

import com.contracthawk.dto.RefreshSummaryDto;
import com.contracthawk.entity.SyncMode;
import com.contracthawk.service.OpenApiSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative endpoints")
public class AdminController {
    
    private final OpenApiSyncService openApiSyncService;
    
    @PostMapping("/refresh-openapi")
    @Operation(summary = "Refresh OpenAPI for all APIs", 
               description = "Fetches and syncs OpenAPI specs for all APIs that have openApiUrl configured. Default mode is MERGE.")
    public ResponseEntity<RefreshSummaryDto> refreshAllOpenApi(
            @RequestParam(defaultValue = "MERGE") SyncMode mode) {
        RefreshSummaryDto summary = openApiSyncService.refreshAll(mode);
        return ResponseEntity.ok(summary);
    }
}

