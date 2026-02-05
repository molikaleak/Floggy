package com.example.floggy.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.floggy.domain.Dependency;
import com.example.floggy.domain.DependencyVersion;
import com.example.floggy.domain.GitHubRepository;
import com.example.floggy.domain.Recommendation;
import com.example.floggy.domain.RiskAssessment;
import com.example.floggy.domain.Vulnerability;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DependencyAnalysisService {
    
    private final GitHubService gitHubService;
    private final VulnerabilityService vulnerabilityService;
    private final GeminiAIService geminiAIService;
    
    @Autowired
    public DependencyAnalysisService(
            GitHubService gitHubService,
            VulnerabilityService vulnerabilityService,
            GeminiAIService geminiAIService) {
        this.gitHubService = gitHubService;
        this.vulnerabilityService = vulnerabilityService;
        this.geminiAIService = geminiAIService;
    }
    
    @Transactional
    public Recommendation analyzeAndRecommend(String packageManager, String dependencyName, 
                                             String currentVersion, String targetVersion) {
        log.info("Analyzing dependency: {}:{} (current: {}, target: {})", 
                packageManager, dependencyName, currentVersion, targetVersion);
        
        // Step 1: Collect data from various sources
        Map<String, Object> githubData = collectGitHubData(dependencyName);
        List<Vulnerability> vulnerabilities = vulnerabilityService.fetchVulnerabilitiesByPackage(
            packageManager, dependencyName, targetVersion);
        Map<String, Object> vulnerabilityData = createVulnerabilityData(vulnerabilities);
        
        // Step 2: Use AI to analyze risk
        Map<String, Object> context = new HashMap<>();
        context.put("github_data", githubData);
        context.put("vulnerability_data", vulnerabilityData);
        context.put("package_manager", packageManager);
        context.put("current_version", currentVersion);
        context.put("target_version", targetVersion);
        
        String aiAnalysis = geminiAIService.analyzeDependencyRisk(
            dependencyName, targetVersion, context);
        
        // Step 3: Generate recommendation
        Map<String, Object> riskFactors = extractRiskFactors(githubData, vulnerabilityData, aiAnalysis);
        String aiRecommendation = geminiAIService.generateRecommendation(
            dependencyName, currentVersion, targetVersion, riskFactors);
        
        // Step 4: Create and return recommendation entity
        Recommendation recommendation = new Recommendation();
        recommendation.setDependency(createOrGetDependency(packageManager, dependencyName));
        recommendation.setCurrentVersion(currentVersion);
        recommendation.setRecommendedVersion(targetVersion);
        recommendation.setReasoning(aiAnalysis + "\n\n" + aiRecommendation);
        recommendation.setConfidenceScore(calculateConfidenceScore(githubData, vulnerabilities, aiAnalysis));
        recommendation.setRiskLevel(determineRiskLevel(vulnerabilities, aiAnalysis));
        recommendation.setRecommendationType(determineRecommendationType(aiRecommendation));
        recommendation.setEstimatedSavingsHours(estimateSavingsHours(vulnerabilities, githubData));
        recommendation.setHasBreakingChanges(checkForBreakingChanges(githubData, aiAnalysis));
        recommendation.setHasSecurityFixes(!vulnerabilities.isEmpty());
        recommendation.setGeneratedBy("AI_HYBRID");
        recommendation.setGeneratedAt(LocalDateTime.now());
        recommendation.setExpiresAt(LocalDateTime.now().plusDays(7));
        recommendation.setIsApplied(false);
        
        return recommendation;
    }
    
    @Transactional
    public RiskAssessment performRiskAssessment(String packageManager, String dependencyName, 
                                               String version) {
        log.info("Performing risk assessment for {}:{}:{}", packageManager, dependencyName, version);
        
        // Collect data
        Map<String, Object> githubData = collectGitHubData(dependencyName);
        List<Vulnerability> vulnerabilities = vulnerabilityService.fetchVulnerabilitiesByPackage(
            packageManager, dependencyName, version);
        
        // Calculate scores
        Double securityScore = vulnerabilityService.calculateSecurityScore(vulnerabilities);
        Double stabilityScore = calculateStabilityScore(githubData);
        Double performanceScore = calculatePerformanceScore(githubData);
        Double overallScore = (securityScore * 0.4 + stabilityScore * 0.4 + performanceScore * 0.2);
        
        // Use AI for ecosystem assessment
        Map<String, Object> vulnerabilityData = createVulnerabilityData(vulnerabilities);
        Map<String, Object> aiAssessment = geminiAIService.assessEcosystemSignals(
            dependencyName, githubData, vulnerabilityData);
        
        // Create risk assessment
        RiskAssessment assessment = new RiskAssessment();
        assessment.setDependencyVersion(createOrGetDependencyVersion(packageManager, dependencyName, version));
        assessment.setAssessmentType("OVERALL");
        assessment.setScore(overallScore);
        assessment.setLevel(determineRiskLevelFromScore(overallScore));
        assessment.setFactors(buildFactorsJson(githubData, vulnerabilities, aiAssessment));
        assessment.setVulnerabilityCount(vulnerabilities.size());
        assessment.setRollbackCount(extractRollbackCount(githubData));
        assessment.setHotfixCount(extractHotfixCount(githubData));
        assessment.setIssueCount(extractIssueCount(githubData));
        assessment.setPullRequestCount(extractPullRequestCount(githubData));
        assessment.setCommunityHealthScore(calculateCommunityHealthScore(githubData));
        assessment.setMaintenanceScore(calculateMaintenanceScore(githubData));
        assessment.setAdoptionScore(calculateAdoptionScore(githubData));
        assessment.setAiExplanation(aiAssessment.toString());
        assessment.setAssessedAt(LocalDateTime.now());
        
        return assessment;
    }
    
    public List<Recommendation> batchAnalyzeDependencies(Map<String, String> dependencies) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            String[] parts = entry.getKey().split(":");
            if (parts.length >= 3) {
                String packageManager = parts[0];
                String dependencyName = parts[1];
                String currentVersion = parts[2];
                String targetVersion = entry.getValue();
                
                try {
                    Recommendation rec = analyzeAndRecommend(
                        packageManager, dependencyName, currentVersion, targetVersion);
                    recommendations.add(rec);
                } catch (Exception e) {
                    log.error("Failed to analyze {}: {}", entry.getKey(), e.getMessage());
                }
            }
        }
        
        return recommendations;
    }
    
    public Map<String, Object> generateDependencyReport(String packageManager, 
                                                       String dependencyName, 
                                                       String version) {
        Map<String, Object> report = new HashMap<>();
        
        // Collect all data
        Map<String, Object> githubData = collectGitHubData(dependencyName);
        List<Vulnerability> vulnerabilities = vulnerabilityService.fetchVulnerabilitiesByPackage(
            packageManager, dependencyName, version);
        RiskAssessment riskAssessment = performRiskAssessment(packageManager, dependencyName, version);
        
        // Build comprehensive report
        report.put("dependency", dependencyName);
        report.put("version", version);
        report.put("package_manager", packageManager);
        report.put("github_data", githubData);
        report.put("vulnerabilities", vulnerabilities);
        report.put("risk_assessment", riskAssessment);
        report.put("security_score", vulnerabilityService.calculateSecurityScore(vulnerabilities));
        report.put("stability_score", calculateStabilityScore(githubData));
        report.put("overall_risk_level", riskAssessment.getLevel());
        report.put("generated_at", LocalDateTime.now());
        
        // Add AI insights
        Map<String, Object> vulnerabilityData = createVulnerabilityData(vulnerabilities);
        Map<String, Object> aiInsights = geminiAIService.assessEcosystemSignals(
            dependencyName, githubData, vulnerabilityData);
        report.put("ai_insights", aiInsights);
        
        return report;
    }
    
    // Helper methods
    
    private Map<String, Object> collectGitHubData(String dependencyName) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            // Search for repositories using this dependency
            List<String> repos = gitHubService.searchDependencyInRepos(dependencyName, 10);
            data.put("dependent_repositories", repos.size());
            data.put("sample_repositories", repos);
            
            if (!repos.isEmpty()) {
                // Analyze the first repository as sample
                String sampleRepo = repos.get(0);
                Optional<GitHubRepository> repoInfo = gitHubService.fetchRepository(sampleRepo);
                repoInfo.ifPresent(repo -> {
                    data.put("sample_repo_stars", repo.getStars());
                    data.put("sample_repo_forks", repo.getForks());
                    data.put("sample_repo_issues", repo.getOpenIssues());
                });
                
                // Get rollback and hotfix counts
                int rollbacks = gitHubService.getRollbackCount(sampleRepo, "any");
                boolean hasHotfix = gitHubService.checkIfVersionHasHotfix(sampleRepo, "any");
                
                data.put("rollback_count", rollbacks);
                data.put("has_hotfixes", hasHotfix);
            }
            
        } catch (Exception e) {
            log.error("Failed to collect GitHub data for {}: {}", dependencyName, e.getMessage());
        }
        
        return data;
    }
    
    private Map<String, Object> createVulnerabilityData(List<Vulnerability> vulnerabilities) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("count", vulnerabilities.size());
        
        Map<String, Integer> severityCount = new HashMap<>();
        for (Vulnerability vuln : vulnerabilities) {
            String severity = vuln.getSeverity();
            severityCount.put(severity, severityCount.getOrDefault(severity, 0) + 1);
        }
        data.put("severity_distribution", severityCount);
        
        // Calculate average CVSS score
        double avgCvss = vulnerabilities.stream()
            .filter(v -> v.getCvssScore() != null)
            .mapToDouble(Vulnerability::getCvssScore)
            .average()
            .orElse(0.0);
        data.put("average_cvss_score", avgCvss);
        
        return data;
    }
    
    private Map<String, Object> extractRiskFactors(Map<String, Object> githubData, 
                                                  Map<String, Object> vulnerabilityData,
                                                  String aiAnalysis) {
        Map<String, Object> factors = new HashMap<>();
        
        factors.put("vulnerability_count", vulnerabilityData.get("count"));
        factors.put("average_cvss_score", vulnerabilityData.get("average_cvss_score"));
        factors.put("dependent_repos", githubData.getOrDefault("dependent_repositories", 0));
        factors.put("rollback_count", githubData.getOrDefault("rollback_count", 0));
        factors.put("has_hotfixes", githubData.getOrDefault("has_hotfixes", false));
        
        // Extract risk level from AI analysis
        if (aiAnalysis.contains("CRITICAL")) {
            factors.put("ai_risk_level", "CRITICAL");
        } else if (aiAnalysis.contains("HIGH")) {
            factors.put("ai_risk_level", "HIGH");
        } else if (aiAnalysis.contains("MEDIUM")) {
            factors.put("ai_risk_level", "MEDIUM");
        } else if (aiAnalysis.contains("LOW")) {
            factors.put("ai_risk_level", "LOW");
        } else {
            factors.put("ai_risk_level", "UNKNOWN");
        }
        
        return factors;
    }
    
    private Dependency createOrGetDependency(String packageManager, String dependencyName) {
        // In a real implementation, this would fetch from database or create if not exists
        Dependency dependency = new Dependency();
        dependency.setPackageManager(packageManager);
        dependency.setArtifactId(dependencyName);
        dependency.setGroupId(packageManager.equals("maven") ? "unknown" : "");
        dependency.setDescription("Auto-generated dependency");
        dependency.setCreatedAt(LocalDateTime.now());
        dependency.setUpdatedAt(LocalDateTime.now());
        return dependency;
    }
    
    private DependencyVersion createOrGetDependencyVersion(String packageManager, 
                                                          String dependencyName, 
                                                          String version) {
        DependencyVersion depVersion = new DependencyVersion();
        depVersion.setVersion(version);
        depVersion.setDependency(createOrGetDependency(packageManager, dependencyName));
        depVersion.setIsLatest(false); // Would need to check
        depVersion.setIsStable(!version.contains("alpha") && !version.contains("beta") && 
                              !version.contains("rc") && !version.contains("SNAPSHOT"));
        depVersion.setAnalyzedAt(LocalDateTime.now());
        depVersion.setCreatedAt(LocalDateTime.now());
        depVersion.setUpdatedAt(LocalDateTime.now());
        return depVersion;
    }
    
    private Double calculateConfidenceScore(Map<String, Object> githubData, 
                                           List<Vulnerability> vulnerabilities,
                                           String aiAnalysis) {
        double score = 70.0; // Base confidence
        
        // Increase confidence based on data availability
        if (!vulnerabilities.isEmpty()) score += 10;
        if (githubData.containsKey("dependent_repositories") && 
            (Integer) githubData.get("dependent_repositories") > 0) score += 10;
        if (aiAnalysis.length() > 100) score += 10;
        
        return Math.min(score, 100.0);
    }
    
    private String determineRiskLevel(List<Vulnerability> vulnerabilities, String aiAnalysis) {
        // Check for critical vulnerabilities
        boolean hasCritical = vulnerabilities.stream()
            .anyMatch(v -> "CRITICAL".equals(v.getSeverity()));
        
        if (hasCritical) return "CRITICAL";
        
        // Check AI analysis for risk indicators
        if (aiAnalysis.contains("CRITICAL")) return "CRITICAL";
        if (aiAnalysis.contains("HIGH")) return "HIGH";
        if (aiAnalysis.contains("MEDIUM")) return "MEDIUM";
        if (aiAnalysis.contains("LOW")) return "LOW";
        
        return "UNKNOWN";
    }
    
    private String determineRecommendationType(String aiRecommendation) {
        if (aiRecommendation.contains("UPGRADE")) return "UPGRADE";
        if (aiRecommendation.contains("HOLD")) return "HOLD";
        if (aiRecommendation.contains("DOWNGRADE")) return "DOWNGRADE";
        if (aiRecommendation.contains("ALTERNATIVE")) return "ALTERNATIVE";
        return "HOLD";
    }
    
    private Integer estimateSavingsHours(List<Vulnerability> vulnerabilities, 
                                        Map<String, Object> githubData) {
        int hours = 0;
        
        // Each vulnerability fixed saves estimated time
        hours += vulnerabilities.size() * 4;
        
        // Rollbacks indicate wasted time
        Integer rollbacks = (Integer) githubData.getOrDefault("rollback_count", 0);
        hours += rollbacks * 8;
        
        return hours;
    }
    
    private Boolean checkForBreakingChanges(Map<String, Object> githubData, String aiAnalysis) {
        // Simple heuristic
        Integer rollbacks = (Integer) githubData.getOrDefault("rollback_count", 0);
        if (rollbacks > 0) return true;
        
        if (aiAnalysis.contains("breaking") || aiAnalysis.contains("Breaking")) return true;
        
        return false;
    }
    
    private Double calculateStabilityScore(Map<String, Object> githubData) {
        double score = 80.0;
        
        Integer rollbacks = (Integer) githubData.getOrDefault("rollback_count", 0);
        Boolean hasHotfixes = (Boolean) githubData.getOrDefault("has_hotfixes", false);
        
        // Deduct for instability indicators
        score -= rollbacks * 5;
        if (hasHotfixes) score -= 10;
        
        return Math.max(score, 0.0);
    }
    
    private Double calculatePerformanceScore(Map<String, Object> githubData) {
        // Placeholder - would need actual performance data
        return 85.0;
    }
    
    private String determineRiskLevelFromScore(Double score) {
        if (score >= 80) return "LOW";
        if (score >= 60) return "MEDIUM";
        if (score >= 40) return "HIGH";
        return "CRITICAL";
    }
    
    private String buildFactorsJson(Map<String, Object> githubData, 
                                   List<Vulnerability> vulnerabilities,
                                   Map<String, Object> aiAssessment) {
        Map<String, Object> factors = new HashMap<>();
        factors.put("github", githubData);
        factors.put("vulnerability_count", vulnerabilities.size());
        factors.put("ai_assessment", aiAssessment);
        
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(factors);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private Integer extractRollbackCount(Map<String, Object> githubData) {
        return (Integer) githubData.getOrDefault("rollback_count", 0);
    }
    
    private Integer extractHotfixCount(Map<String, Object> githubData) {
        Boolean hasHotfixes = (Boolean) githubData.getOrDefault("has_hotfixes", false);
        return hasHotfixes ? 1 : 0;
    }
    
    private Integer extractIssueCount(Map<String, Object> githubData) {
        return (Integer) githubData.getOrDefault("sample_repo_issues", 0);
    }
    
    private Integer extractPullRequestCount(Map<String, Object> githubData) {
        // Placeholder - would need actual PR count
        return 0;
    }
    
    private Double calculateCommunityHealthScore(Map<String, Object> githubData) {
        double score = 70.0;
        
        Integer stars = (Integer) githubData.getOrDefault("sample_repo_stars", 0);
        Integer forks = (Integer) githubData.getOrDefault("sample_repo_forks", 0);
        
        if (stars > 1000) score += 20;
        else if (stars > 100) score += 10;
        
        if (forks > 100) score += 10;
        
        return Math.min(score, 100.0);
    }
    
    private Double calculateMaintenanceScore(Map<String, Object> githubData) {
        double score = 75.0;
        
        Integer issues = (Integer) githubData.getOrDefault("sample_repo_issues", 0);
        if (issues > 100) score -= 20;
        else if (issues > 50) score -= 10;
        
        return Math.max(score, 0.0);
    }
    
    private Double calculateAdoptionScore(Map<String, Object> githubData) {
        Integer dependentRepos = (Integer) githubData.getOrDefault("dependent_repositories", 0);
        
        if (dependentRepos > 1000) return 95.0;
        if (dependentRepos > 100) return 80.0;
        if (dependentRepos > 10) return 65.0;
        return 50.0;
    }
}