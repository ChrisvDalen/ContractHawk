package com.contracthawk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "api_contract", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"name", "owner_team"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiContract {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 80)
    private String name;
    
    @Column(name = "base_url", nullable = false, length = 200)
    private String baseUrl;
    
    @Column(nullable = false, length = 40)
    private String version;
    
    @Column(name = "owner_team", nullable = false, length = 60)
    private String ownerTeam;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Lifecycle lifecycle;
    
    @Column(name = "open_api_url", length = 300)
    private String openApiUrl;
    
    @Column(length = 400)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "apiContract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Endpoint> endpoints = new ArrayList<>();
    
    @OneToMany(mappedBy = "apiContract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChangelogEntry> changelogEntries = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

