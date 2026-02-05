package com.example.floggy.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.floggy.domain.GitHubRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GitHubService {
    
    private final GitHub gitHub;
    
    @Autowired
    public GitHubService(GitHub gitHub) {
        this.gitHub = gitHub;
    }
    
    public Optional<GitHubRepository> fetchRepository(String repoFullName) {
        try {
            GHRepository ghRepo = gitHub.getRepository(repoFullName);
            
            GitHubRepository repository = new GitHubRepository();
            repository.setFullName(ghRepo.getFullName());
            repository.setOwner(ghRepo.getOwnerName());
            repository.setName(ghRepo.getName());
            repository.setDescription(ghRepo.getDescription());
            repository.setUrl(ghRepo.getHtmlUrl().toString());
            repository.setStars(ghRepo.getStargazersCount());
            repository.setForks(ghRepo.getForksCount());
            repository.setWatchers(ghRepo.getWatchersCount());
            repository.setOpenIssues(ghRepo.getOpenIssueCount());
            repository.setLanguage(ghRepo.getLanguage());
            
            if (ghRepo.getPushedAt() != null) {
                repository.setLastPushed(
                    ghRepo.getPushedAt().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                );
            }
            
            repository.setCreatedAt(
                ghRepo.getCreatedAt().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            );
            repository.setUpdatedAt(LocalDateTime.now());
            repository.setIsMonitored(true);
            
            return Optional.of(repository);
            
        } catch (IOException e) {
            log.error("Failed to fetch repository {}: {}", repoFullName, e.getMessage());
            return Optional.empty();
        }
    }
    
    public List<GHIssue> fetchRecentIssues(String repoFullName, int count) {
        try {
            GHRepository ghRepo = gitHub.getRepository(repoFullName);
            List<GHIssue> issues = new ArrayList<>();
            int i = 0;
            for (GHIssue issue : ghRepo.getIssues(GHIssueState.ALL)) {
                if (i >= count) break;
                issues.add(issue);
                i++;
            }
            return issues;
        } catch (IOException e) {
            log.error("Failed to fetch issues for {}: {}", repoFullName, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<GHPullRequest> fetchRecentPullRequests(String repoFullName, int count) {
        try {
            GHRepository ghRepo = gitHub.getRepository(repoFullName);
            List<GHPullRequest> prs = new ArrayList<>();
            int i = 0;
            for (GHPullRequest pr : ghRepo.getPullRequests(GHIssueState.ALL)) {
                if (i >= count) break;
                prs.add(pr);
                i++;
            }
            return prs;
        } catch (IOException e) {
            log.error("Failed to fetch pull requests for {}: {}", repoFullName, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<GHCommit> fetchRecentCommits(String repoFullName, int count) {
        try {
            GHRepository ghRepo = gitHub.getRepository(repoFullName);
            List<GHCommit> commits = new ArrayList<>();
            int i = 0;
            for (GHCommit commit : ghRepo.listCommits()) {
                if (i >= count) break;
                commits.add(commit);
                i++;
            }
            return commits;
        } catch (IOException e) {
            log.error("Failed to fetch commits for {}: {}", repoFullName, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public int getRollbackCount(String repoFullName, String version) {
        try {
            GHRepository ghRepo = gitHub.getRepository(repoFullName);
            List<GHCommit> commits = ghRepo.listCommits().asList();
            
            // Simple heuristic: count commits that mention "revert", "rollback", "downgrade"
            int rollbackCount = 0;
            for (GHCommit commit : commits) {
                String message = commit.getCommitShortInfo().getMessage().toLowerCase();
                if (message.contains("revert") || message.contains("rollback") || 
                    message.contains("downgrade") || message.contains("back out")) {
                    rollbackCount++;
                }
            }
            return rollbackCount;
        } catch (IOException e) {
            log.error("Failed to analyze rollbacks for {}: {}", repoFullName, e.getMessage());
            return 0;
        }
    }
    
    public boolean checkIfVersionHasHotfix(String repoFullName, String version) {
        try {
            GHRepository ghRepo = gitHub.getRepository(repoFullName);
            List<GHCommit> commits = ghRepo.listCommits().asList();
            
            for (GHCommit commit : commits) {
                String message = commit.getCommitShortInfo().getMessage().toLowerCase();
                if ((message.contains("hotfix") || message.contains("patch")) && 
                    message.contains(version)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to check hotfixes for {}: {}", repoFullName, e.getMessage());
            return false;
        }
    }
    
    public List<String> searchDependencyInRepos(String dependencyName, int maxRepos) {
        try {
            PagedSearchIterable<GHRepository> search = gitHub.searchRepositories()
                .q(dependencyName + " in:file extension:json extension:xml extension:gradle extension:py")
                .list();
            
            List<String> repos = new ArrayList<>();
            int count = 0;
            for (GHRepository repo : search) {
                if (count >= maxRepos) break;
                repos.add(repo.getFullName());
                count++;
            }
            return repos;
        } catch (Exception e) {
            log.error("Failed to search for dependency {}: {}", dependencyName, e.getMessage());
            return new ArrayList<>();
        }
    }
}