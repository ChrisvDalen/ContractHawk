package com.contracthawk.dto;

import com.contracthawk.entity.Lifecycle;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ApiCreateDto {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters")
    private String name;
    
    @NotBlank(message = "Base URL is required")
    @Size(max = 200, message = "Base URL must not exceed 200 characters")
    private String baseUrl;
    
    @NotBlank(message = "Version is required")
    @Size(max = 40, message = "Version must not exceed 40 characters")
    private String version;
    
    @NotBlank(message = "Owner team is required")
    @Size(max = 60, message = "Owner team must not exceed 60 characters")
    private String ownerTeam;
    
    @NotNull(message = "Lifecycle is required")
    private Lifecycle lifecycle;
    
    @Size(max = 300, message = "OpenAPI URL must not exceed 300 characters")
    private String openApiUrl;
    
    @Size(max = 400, message = "Description must not exceed 400 characters")
    private String description;
}

