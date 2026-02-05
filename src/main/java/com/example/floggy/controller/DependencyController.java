package com.example.floggy.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.floggy.domain.Recommendation;
import com.example.floggy.domain.RiskAssessment;
import com.example.floggy.service.DependencyAnalysisService;
import com.example.floggy.service.GitHubService;
import com.example.floggy.service.VulnerabilityService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/dependencies")
@Slf4j
public class DependencyController {
    
    private final DependencyAnalysisService analysisService;
    private final GitHubService gitHubService;
    private final VulnerabilityService vulnerabilityService;
    
    @Autowired
    public DependencyController(
            DependencyAnalysisService analysisService,
            GitHubService gitHubService,
            VulnerabilityService vulnerabilityService) {
        this.analysisService = analysisService;
        this.gitHubService = gitHubService;
        this.vulnerabilityService = vulnerabilityService;
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<Recommendation> analyzeDependency(
            @RequestParam String packageManager,
            @RequestParam String dependency,
            @RequestParam String currentVersion,
            @RequestParam String targetVersion) {
        
        log.info("Analyzing dependency: {}:{} from {} to {}", 
                packageManager, dependency, currentVersion, targetVersion);
        
        Recommendation recommendation = analysisService.analyzeAndRecommend(
            packageManager, dependency, currentVersion, targetVersion);
        
        return ResponseEntity.ok(recommendation);
    }
    
    @GetMapping("/risk-assessment")
    public ResponseEntity<RiskAssessment> getRiskAssessment(
            @RequestParam String packageManager,
            @RequestParam String dependency,
            @RequestParam String version) {
        
        log.info("Getting risk assessment for {}:{}:{}", packageManager, dependency, version);
        
        RiskAssessment assessment = analysisService.performRiskAssessment(
            packageManager, dependency, version);
        
        return ResponseEntity.ok(assessment);
    }
    
    @PostMapping("/batch-analyze")
    public ResponseEntity<List<Recommendation>> batchAnalyze(
            @RequestBody Map<String, String> dependencies) {
        
        log.info("Batch analyzing {} dependencies", dependencies.size());
        
        List<Recommendation> recommendations = analysisService.batchAnalyzeDependencies(dependencies);
        
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestParam String packageManager,
            @RequestParam String dependency,
            @RequestParam String version) {
        
        log.info("Generating report for {}:{}:{}", packageManager, dependency, version);
        
        Map<String, Object> report = analysisService.generateDependencyReport(
            packageManager, dependency, version);
        
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/vulnerabilities")
    public ResponseEntity<Map<String, Object>> getVulnerabilities(
            @RequestParam String packageManager,
            @RequestParam String dependency,
            @RequestParam(required = false) String version) {
        
        log.info("Getting vulnerabilities for {}:{}:{}", packageManager, dependency, version);
        
        List<com.example.floggy.domain.Vulnerability> vulnerabilities = 
            vulnerabilityService.fetchVulnerabilitiesByPackage(packageManager, dependency, version);
        
        Map<String, Object> response = new HashMap<>();
        response.put("dependency", dependency);
        response.put("version", version);
        response.put("vulnerabilities", vulnerabilities);
        response.put("count", vulnerabilities.size());
        response.put("security_score", vulnerabilityService.calculateSecurityScore(vulnerabilities));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/github-info")
    public ResponseEntity<Map<String, Object>> getGitHubInfo(
            @RequestParam String dependency) {
        
        log.info("Getting GitHub info for {}", dependency);
        
        Map<String, Object> githubData = new HashMap<>();
        
        try {
            // Search for repositories using this dependency
            List<String> repos = gitHubService.searchDependencyInRepos(dependency, 5);
            githubData.put("dependent_repositories", repos);
            githubData.put("dependent_repo_count", repos.size());
            
            if (!repos.isEmpty()) {
                // Get info for the first repository
                String sampleRepo = repos.get(0);
                githubData.put("sample_repository", sampleRepo);
                
                var repoInfo = gitHubService.fetchRepository(sampleRepo);
                repoInfo.ifPresent(repo -> {
                    githubData.put("stars", repo.getStars());
                    githubData.put("forks", repo.getForks());
                    githubData.put("open_issues", repo.getOpenIssues());
                    githubData.put("language", repo.getLanguage());
                    githubData.put("last_pushed", repo.getLastPushed());
                });
                
                // Get rollback count
                int rollbacks = gitHubService.getRollbackCount(sampleRepo, "any");
                githubData.put("rollback_count", rollbacks);
                
                // Check for hotfixes
                boolean hasHotfix = gitHubService.checkIfVersionHasHotfix(sampleRepo, "any");
                githubData.put("has_hotfixes", hasHotfix);
            }
            
        } catch (Exception e) {
            log.error("Failed to get GitHub info: {}", e.getMessage());
            githubData.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(githubData);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Smart Dependency Advisor");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/simulate-project")
    public ResponseEntity<Map<String, Object>> simulateProjectAnalysis(
            @RequestBody List<String> dependencies) {
        
        log.info("Simulating project analysis for {} dependencies", dependencies.size());
        
        Map<String, Object> projectAnalysis = new HashMap<>();
        Map<String, RiskAssessment> riskAssessments = new HashMap<>();
        int totalVulnerabilities = 0;
        double averageRiskScore = 0.0;
        
        for (String dep : dependencies) {
            try {
                String[] parts = dep.split(":");
                if (parts.length >= 3) {
                    String packageManager = parts[0];
                    String dependencyName = parts[1];
                    String version = parts[2];
                    
                    // Get risk assessment
                    RiskAssessment assessment = analysisService.performRiskAssessment(
                        packageManager, dependencyName, version);
                    riskAssessments.put(dep, assessment);
                    
                    // Get vulnerabilities
                    List<com.example.floggy.domain.Vulnerability> vulnerabilities = 
                        vulnerabilityService.fetchVulnerabilitiesByPackage(packageManager, dependencyName, version);
                    totalVulnerabilities += vulnerabilities.size();
                    
                    // Calculate average risk score
                    averageRiskScore += assessment.getScore() != null ? assessment.getScore() : 0;
                }
            } catch (Exception e) {
                log.error("Failed to analyze {}: {}", dep, e.getMessage());
            }
        }
        
        if (!riskAssessments.isEmpty()) {
            averageRiskScore /= riskAssessments.size();
        }
        
        // Determine overall project risk
        String overallRisk = "LOW";
        if (averageRiskScore < 40) overallRisk = "CRITICAL";
        else if (averageRiskScore < 60) overallRisk = "HIGH";
        else if (averageRiskScore < 80) overallRisk = "MEDIUM";
        
        projectAnalysis.put("dependencies_analyzed", dependencies.size());
        projectAnalysis.put("total_vulnerabilities", totalVulnerabilities);
        projectAnalysis.put("average_risk_score", averageRiskScore);
        projectAnalysis.put("overall_project_risk", overallRisk);
        projectAnalysis.put("risk_assessments", riskAssessments);
        projectAnalysis.put("recommendations", "Run individual analysis for upgrade recommendations");
        
        return ResponseEntity.ok(projectAnalysis);
    }
}