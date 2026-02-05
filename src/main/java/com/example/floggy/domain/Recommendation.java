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
@Table(name = "recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependency_id", nullable = false)
    private Dependency dependency;
    
    @Column(nullable = false)
    private String currentVersion;
    
    @Column(nullable = false)
    private String recommendedVersion;
    
    @Column
    private String alternativeVersion; // Alternative safe version
    
    @Column(length = 5000)
    private String reasoning;
    
    @Column
    private Double confidenceScore; // 0-100
    
    @Column
    private String riskLevel; // LOW, MEDIUM, HIGH
    
    @Column
    private String recommendationType; // UPGRADE, HOLD, DOWNGRADE, ALTERNATIVE
    
    @Column
    private Integer estimatedSavingsHours; // Estimated developer hours saved
    
    @Column
    private Boolean hasBreakingChanges;
    
    @Column
    private Boolean hasSecurityFixes;
    
    @Column
    private Boolean hasPerformanceImprovements;
    
    @Column
    private Boolean hasBugFixes;
    
    @Column
    private String generatedBy; // AI, RULE_BASED, HYBRID
    
    @Column
    private LocalDateTime generatedAt;
    
    @Column
    private LocalDateTime expiresAt;
    
    @Column
    private Boolean isApplied;
    
    @Column
    private LocalDateTime appliedAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        generatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}