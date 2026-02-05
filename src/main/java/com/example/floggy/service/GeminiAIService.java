package com.example.floggy.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeminiAIService {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${gemini.api.key:}")
    private String apiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent}")
    private String apiUrl;
    
    public GeminiAIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }
    
    public String analyzeDependencyRisk(String dependencyName, String version, 
                                        Map<String, Object> context) {
        try {
            String prompt = buildRiskAnalysisPrompt(dependencyName, version, context);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            
            contents.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{contents});
            
            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.2);
            generationConfig.put("topP", 0.8);
            generationConfig.put("topK", 40);
            generationConfig.put("maxOutputTokens", 2048);
            requestBody.put("generationConfig", generationConfig);
            
            String response = webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            return extractAnalysisFromResponse(response);
            
        } catch (Exception e) {
            log.error("Failed to analyze dependency risk with Gemini AI: {}", e.getMessage());
            return "AI analysis unavailable. Please check manually.";
        }
    }
    
    public String generateRecommendation(String dependencyName, String currentVersion, 
                                         String availableVersions, Map<String, Object> riskFactors) {
        try {
            String prompt = buildRecommendationPrompt(dependencyName, currentVersion, 
                                                     availableVersions, riskFactors);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            
            contents.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{contents});
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.3);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);
            
            String response = webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            return extractRecommendationFromResponse(response);
            
        } catch (Exception e) {
            log.error("Failed to generate recommendation with Gemini AI: {}", e.getMessage());
            return "Hold: Unable to generate AI recommendation. Please review manually.";
        }
    }
    
    public Map<String, Object> assessEcosystemSignals(String dependencyName, 
                                                      Map<String, Object> githubData,
                                                      Map<String, Object> vulnerabilityData) {
        try {
            String prompt = buildEcosystemAnalysisPrompt(dependencyName, githubData, vulnerabilityData);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            
            contents.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{contents});
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.1);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);
            
            String response = webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            return parseAssessmentResponse(response);
            
        } catch (Exception e) {
            log.error("Failed to assess ecosystem signals with Gemini AI: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("risk_level", "UNKNOWN");
            fallback.put("confidence", 0.0);
            fallback.put("summary", "AI assessment unavailable");
            return fallback;
        }
    }
    
    private String buildRiskAnalysisPrompt(String dependencyName, String version, 
                                          Map<String, Object> context) {
        return String.format("""
            You are a senior software dependency security analyst. Analyze the risk of updating to %s version %s.
            
            Context:
            %s
            
            Please provide a comprehensive risk analysis including:
            1. Security risk level (CRITICAL, HIGH, MEDIUM, LOW, NONE)
            2. Stability assessment
            3. Breaking changes likelihood
            4. Community adoption signals
            5. Recommended action (UPGRADE, HOLD, AVOID)
            6. Detailed reasoning
            
            Format your response as JSON with these fields:
            - risk_level
            - security_score (0-100)
            - stability_score (0-100)
            - breaking_changes_risk (LOW, MEDIUM, HIGH)
            - recommended_action
            - reasoning
            - confidence (0-100)
            """, dependencyName, version, context.toString());
    }
    
    private String buildRecommendationPrompt(String dependencyName, String currentVersion,
                                            String availableVersions, Map<String, Object> riskFactors) {
        return String.format("""
            As a dependency management expert, recommend the best version to upgrade to for %s.
            
            Current version: %s
            Available versions: %s
            Risk factors: %s
            
            Consider:
            1. Security vulnerabilities
            2. Stability and bug fixes
            3. Breaking changes
            4. Community adoption
            5. Release age
            
            Provide a clear recommendation in this format:
            - Recommended version:
            - Alternative version (if applicable):
            - Action: UPGRADE, HOLD, or DOWNGRADE
            - Expected benefits:
            - Potential risks:
            - Estimated developer hours saved:
            """, dependencyName, currentVersion, availableVersions, riskFactors.toString());
    }
    
    private String buildEcosystemAnalysisPrompt(String dependencyName, 
                                               Map<String, Object> githubData,
                                               Map<String, Object> vulnerabilityData) {
        return String.format("""
            Analyze ecosystem health signals for dependency: %s
            
            GitHub Data:
            %s
            
            Vulnerability Data:
            %s
            
            Analyze these signals to determine overall dependency health.
            Provide assessment as JSON with:
            - overall_health_score (0-100)
            - maintenance_activity (ACTIVE, MODERATE, LOW, STALE)
            - community_engagement (HIGH, MEDIUM, LOW)
            - security_posture (STRONG, MODERATE, WEAK)
            - release_frequency (FREQUENT, REGULAR, INFREQUENT, STALE)
            - risk_trend (IMPROVING, STABLE, WORSENING)
            - key_risks (array of strings)
            - recommendations (array of strings)
            """, dependencyName, githubData.toString(), vulnerabilityData.toString());
    }
    
    private String extractAnalysisFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            return "No analysis generated";
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            return "Error parsing AI response";
        }
    }
    
    private String extractRecommendationFromResponse(String response) {
        return extractAnalysisFromResponse(response); // Same extraction logic
    }
    
    private Map<String, Object> parseAssessmentResponse(String response) {
        Map<String, Object> result = new HashMap<>();
        try {
            String text = extractAnalysisFromResponse(response);
            // Try to parse as JSON
            if (text.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(text);
                jsonNode.fields().forEachRemaining(entry -> {
                    result.put(entry.getKey(), entry.getValue().asText());
                });
            } else {
                // Fallback to simple parsing
                result.put("assessment", text);
            }
        } catch (Exception e) {
            log.error("Failed to parse assessment response: {}", e.getMessage());
            result.put("error", "Failed to parse AI assessment");
        }
        return result;
    }
    
    public Double calculateAIRiskScore(String analysisText) {
        // Simple heuristic to convert text analysis to numeric score
        if (analysisText.contains("CRITICAL") || analysisText.contains("HIGH")) {
            return 25.0;
        } else if (analysisText.contains("MEDIUM")) {
            return 50.0;
        } else if (analysisText.contains("LOW")) {
            return 75.0;
        } else {
            return 90.0;
        }
    }
}