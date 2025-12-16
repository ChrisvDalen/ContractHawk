 package com.contracthawk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "changelog_entry")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiContract apiContract;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangelogType type;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean breaking = false;
    
    @Column(nullable = false, length = 200)
    private String summary;
    
    @Column(length = 1000)
    private String details;
    
    @Column(name = "released_at", nullable = false)
    private Instant releasedAt;
}

