package com.contracthawk.dto;

import com.contracthawk.entity.ChangelogType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;

@Data
public class ChangelogCreateDto {
    
    @NotNull(message = "Type is required")
    private ChangelogType type;
    
    private Boolean breaking = false;
    
    @NotBlank(message = "Summary is required")
    @Size(max = 200, message = "Summary must not exceed 200 characters")
    private String summary;
    
    @Size(max = 1000, message = "Details must not exceed 1000 characters")
    private String details;
    
    @NotNull(message = "Released at is required")
    private Instant releasedAt;
}

