package com.example.floggy.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "risk_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependency_version_id", nullable = false)
    private DependencyVersion dependencyVersion;
    
    @Column(nullable = false)
    private String assessmentType; // SECURITY, STABILITY, PERFORMANCE, OVERALL
    
    @Column
    private Double score; // 0-100
    
    @Column
    private String level; // LOW, MEDIUM, HIGH, CRITICAL
    
    @Column(length = 5000)
    private String factors; // JSON or comma-separated factors
    
    @Column
    private Integer vulnerabilityCount;
    
    @Column
    private Integer rollbackCount;
    
    @Column
    private Integer hotfixCount;
    
    @Column
    private Integer issueCount;
    
    @Column
    private Integer pullRequestCount;
    
    @Column
    private Double communityHealthScore;
    
    @Column
    private Double maintenanceScore;
    
    @Column
    private Double adoptionScore;
    
    @Column(length = 5000)
    private String aiExplanation;
    
    @Column
    private LocalDateTime assessedAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        assessedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}