package com.contracthawk.dto;

import com.contracthawk.entity.ChangelogType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogDto {
    private UUID id;
    private ChangelogType type;
    private Boolean breaking;
    private String summary;
    private String details;
    private Instant releasedAt;
}

