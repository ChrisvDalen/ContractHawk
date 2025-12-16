package com.contracthawk.dto;

import com.contracthawk.entity.SyncMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OpenApiImportRequestDto {
    
    @NotNull(message = "Mode is required")
    private SyncMode mode;
}

