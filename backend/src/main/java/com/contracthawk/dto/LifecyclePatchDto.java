package com.contracthawk.dto;

import com.contracthawk.entity.Lifecycle;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LifecyclePatchDto {
    
    @NotNull(message = "Lifecycle is required")
    private Lifecycle lifecycle;
}

