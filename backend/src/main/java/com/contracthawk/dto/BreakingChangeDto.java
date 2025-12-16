package com.contracthawk.dto;

import com.contracthawk.entity.BreakingChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakingChangeDto {
    private BreakingChangeType type;
    private String method;
    private String path;
    private String details;
}

