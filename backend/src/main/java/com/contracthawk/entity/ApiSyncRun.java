package com.contracthawk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_sync_run")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSyncRun {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiContract apiContract;
    
    @Column(name = "run_at", nullable = false)
    private Instant runAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncMode mode;
    
    @Column(name = "added_count", nullable = false)
    @Builder.Default
    private Integer addedCount = 0;
    
    @Column(name = "updated_count", nullable = false)
    @Builder.Default
    private Integer updatedCount = 0;
    
    @Column(name = "deleted_count", nullable = false)
    @Builder.Default
    private Integer deletedCount = 0;
    
    @Column(name = "breaks_detected", nullable = false)
    @Builder.Default
    private Boolean breaksDetected = false;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
}

