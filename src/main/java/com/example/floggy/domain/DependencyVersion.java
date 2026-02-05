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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dependency_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependencyVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependency_id", nullable = false)
    private Dependency dependency;
    
    @Column(nullable = false)
    private String version; // Semantic version
    
    @Column
    private LocalDateTime releaseDate;
    
    @Column
    private Boolean isLatest;
    
    @Column
    private Boolean isStable;
    
    @Column
    private Integer downloadCount;
    
    @Column(length = 5000)
    private String releaseNotes;
    
    @Column
    private Integer githubStars;
    
    @Column
    private Integer githubForks;
    
    @Column
    private Integer githubIssues;
    
    @Column
    private Integer githubPullRequests;
    
    @Column
    private Integer rollbackCount; // Number of times this version was rolled back
    
    @Column
    private Integer hotfixCount; // Number of hotfixes after release
    
    @Column
    private Double stabilityScore; // 0-100
    
    @Column
    private Double securityScore; // 0-100
    
    @Column
    private Double performanceScore; // 0-100
    
    @Column
    private Double overallScore; // 0-100
    
    @Column
    private String recommendation; // RECOMMENDED, CAUTION, AVOID
    
    @OneToMany(mappedBy = "dependencyVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vulnerability> vulnerabilities;
    
    @Column
    private LocalDateTime analyzedAt;
    
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