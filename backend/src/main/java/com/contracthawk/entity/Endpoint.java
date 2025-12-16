package com.contracthawk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "endpoint",
       uniqueConstraints = @UniqueConstraint(columnNames = {"api_id", "method", "path"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Endpoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiContract apiContract;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private HttpMethod method;
    
    @Column(nullable = false, length = 200)
    private String path;
    
    @Column(length = 300)
    private String description;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean deprecated = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

