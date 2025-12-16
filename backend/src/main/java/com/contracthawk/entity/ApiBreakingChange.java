package com.contracthawk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "api_breaking_change")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiBreakingChange {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_run_id", nullable = false)
    private ApiSyncRun syncRun;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BreakingChangeType type;
    
    @Column(nullable = false, length = 10)
    private String method;
    
    @Column(nullable = false, length = 200)
    private String path;
    
    @Column(length = 300)
    private String details;
}

