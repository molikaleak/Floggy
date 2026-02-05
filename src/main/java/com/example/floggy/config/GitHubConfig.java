package com.example.floggy.config;

import java.io.IOException;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitHubConfig {
    
    @Value("${github.token:}")
    private String githubToken;
    
    @Value("${github.api.url:https://api.github.com}")
    private String githubApiUrl;
    
    @Bean
    public GitHub gitHubClient() throws IOException {
        GitHubBuilder builder = new GitHubBuilder();
        
        if (githubToken != null && !githubToken.isEmpty()) {
            builder.withOAuthToken(githubToken);
        }
        
        builder.withEndpoint(githubApiUrl);
        
        return builder.build();
    }
}