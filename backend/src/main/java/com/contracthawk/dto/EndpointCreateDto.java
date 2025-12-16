package com.contracthawk.dto;

import com.contracthawk.entity.HttpMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EndpointCreateDto {
    
    @NotNull(message = "Method is required")
    private HttpMethod method;
    
    @NotBlank(message = "Path is required")
    @Size(min = 1, max = 200, message = "Path must be between 1 and 200 characters")
    private String path;
    
    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;
    
    private Boolean deprecated = false;
}

