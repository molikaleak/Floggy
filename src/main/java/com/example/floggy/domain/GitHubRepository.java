package com.example.floggy.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_repositories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String fullName; // owner/repo
    
    @Column
    private String owner;
    
    @Column
    private String name;
    
    @Column
    private String description;
    
    @Column
    private String url;
    
    @Column
    private Integer stars;
    
    @Column
    private Integer forks;
    
    @Column
    private Integer watchers;
    
    @Column
    private Integer openIssues;
    
    @Column
    private String language;
    
    @Column
    private LocalDateTime lastPushed;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private Boolean isMonitored;
    
    @Column
    private Integer dependencyCount;
    
    @Column
    private Integer vulnerableDependencyCount;
    
    @Column
    private Double repositoryRiskScore;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}