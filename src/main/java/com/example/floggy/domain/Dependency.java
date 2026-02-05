package com.example.floggy.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dependencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dependency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String packageManager; // npm, maven, gradle, pip, etc.
    
    @Column(nullable = false)
    private String groupId; // For Maven, optional for others
    
    @Column(nullable = false)
    private String artifactId; // Package name
    
    @Column
    private String description;
    
    @Column
    private String homepage;
    
    @Column
    private String repositoryUrl;
    
    @OneToMany(mappedBy = "dependency", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DependencyVersion> versions;
    
    @Column
    private Integer totalDownloads;
    
    @Column
    private LocalDateTime lastUpdated;
    
    @Column
    private Double riskScore; // 0-100, higher = more risky
    
    @Column
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
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