# Entity Mapping Documentation for Floggy

This document provides a comprehensive mapping between Java entity classes and PostgreSQL database tables for the Floggy dependency analysis system.

## Database Overview

- **Database Name**: `floggy`
- **Schema**: `public` (default)
- **User**: `floggy`
- **Connection**: PostgreSQL 16+

## Entity to Table Mapping

### 1. Dependency Entity → `dependencies` table

**Java Class**: `com.example.floggy.domain.Dependency`
**Table Name**: `dependencies`

| Java Field | Column Name | Type | Constraints | Description |
|------------|-------------|------|-------------|-------------|
| id | id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| packageManager | package_manager | VARCHAR(50) | NOT NULL | Package manager (npm, maven, gradle, pip, etc.) |
| groupId | group_id | VARCHAR(255) | NOT NULL | Group ID (for Maven, optional for others) |
| artifactId | artifact_id | VARCHAR(255) | NOT NULL | Package/artifact name |
| description | description | TEXT | NULL | Package description |
| homepage | homepage | VARCHAR(500) | NULL | Project homepage URL |
| repositoryUrl | repository_url | VARCHAR(500) | NULL | Source repository URL |
| totalDownloads | total_downloads | INTEGER | NULL | Total download count |
| lastUpdated | last_updated | TIMESTAMP | NULL | Last update timestamp |
| riskScore | risk_score | DOUBLE PRECISION | NULL | Risk score (0-100) |
| riskLevel | risk_level | VARCHAR(20) | CHECK constraint | Risk level: LOW, MEDIUM, HIGH, CRITICAL |
| createdAt | created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updatedAt | updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Unique Constraint**: `(package_manager, group_id, artifact_id)`
**Relationships**: One-to-many with `DependencyVersion`

### 2. DependencyVersion Entity → `dependency_versions` table

**Java Class**: `com.example.floggy.domain.DependencyVersion`
**Table Name**: `dependency_versions`

| Java Field | Column Name | Type | Constraints | Description |
|------------|-------------|------|-------------|-------------|
| id | id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| dependency | dependency_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to parent dependency |
| version | version | VARCHAR(100) | NOT NULL | Semantic version string |
| releaseDate | release_date | TIMESTAMP | NULL | Version release date |
| isLatest | is_latest | BOOLEAN | NULL | Whether this is the latest version |
| isStable | is_stable | BOOLEAN | NULL | Whether this is a stable version |
| downloadCount | download_count | INTEGER | NULL | Version-specific download count |
| releaseNotes | release_notes | TEXT | NULL | Release notes/change log |
| githubStars | github_stars | INTEGER | NULL | GitHub stars count |
| githubForks | github_forks | INTEGER | NULL | GitHub forks count |
| githubIssues | github_issues | INTEGER | NULL | Open issues count |
| githubPullRequests | github_pull_requests | INTEGER | NULL | Open pull requests count |
| rollbackCount | rollback_count | INTEGER | NULL | Number of rollbacks for this version |
| hotfixCount | hotfix_count | INTEGER | NULL | Number of hotfixes after release |
| stabilityScore | stability_score | DOUBLE PRECISION | NULL | Stability score (0-100) |
| securityScore | security_score | DOUBLE PRECISION | NULL | Security score (0-100) |
| performanceScore | performance_score | DOUBLE PRECISION | NULL | Performance score (0-100) |
| overallScore | overall_score | DOUBLE PRECISION | NULL | Overall score (0-100) |
| recommendation | recommendation | VARCHAR(20) | CHECK constraint | Recommendation: RECOMMENDED, CAUTION, AVOID |
| analyzedAt | analyzed_at | TIMESTAMP | NULL | When this version was analyzed |
| createdAt | created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updatedAt | updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Foreign Key**: `dependency_id` references `dependencies(id)` ON DELETE CASCADE
**Unique Constraint**: `(dependency_id, version)`
**Relationships**: Many-to-one with `Dependency`, One-to-many with `Vulnerability` and `RiskAssessment`

### 3. Vulnerability Entity → `vulnerabilities` table

**Java Class**: `com.example.floggy.domain.Vulnerability`
**Table Name**: `vulnerabilities`

| Java Field | Column Name | Type | Constraints | Description |
|------------|-------------|------|-------------|-------------|
| id | id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| dependencyVersion | dependency_version_id | BIGINT | NULL, FOREIGN KEY | Reference to affected dependency version |
| source | source | VARCHAR(50) | NOT NULL | Vulnerability source (NVD, GitHub Advisory, Snyk, etc.) |
| vulnerabilityId | vulnerability_id | VARCHAR(100) | NOT NULL | Vulnerability ID (CVE-2023-12345, GHSA-xxxx) |
| severity | severity | VARCHAR(20) | NOT NULL, CHECK constraint | Severity: LOW, MEDIUM, HIGH, CRITICAL |
| cvssScore | cvss_score | DOUBLE PRECISION | NULL | CVSS score (0.0-10.0) |
| description | description | TEXT | NULL | Vulnerability description |
| affectedVersions | affected_versions | VARCHAR(500) | NULL | Affected version range |
| patchedVersions | patched_versions | VARCHAR(500) | NULL | Patched version range |
| publishedDate | published_date | TIMESTAMP | NULL | Publication date |
| lastModifiedDate | last_modified_date | TIMESTAMP | NULL | Last modification date |
| references | references | TEXT | NULL | Reference URLs |
| isExploited | is_exploited | BOOLEAN | NULL | Whether actively exploited |
| hasFix | has_fix | BOOLEAN | NULL | Whether a fix is available |
| createdAt | created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updatedAt | updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Foreign Key**: `dependency_version_id` references `dependency_versions(id)` ON DELETE CASCADE
**Unique Constraint**: `(source, vulnerability_id, dependency_version_id)`

### 4. GitHubRepository Entity → `github_repositories` table

**Java Class**: `com.example.floggy.domain.GitHubRepository`
**Table Name**: `github_repositories`

| Java Field | Column Name | Type | Constraints | Description |
|------------|-------------|------|-------------|-------------|
| id | id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| fullName | full_name | VARCHAR(255) | NOT NULL UNIQUE | Repository full name (owner/repo) |
| owner | owner | VARCHAR(100) | NULL | Repository owner |
| name | name | VARCHAR(100) | NULL | Repository name |
| description | description | TEXT | NULL | Repository description |
| url | url | VARCHAR(500) | NULL | Repository URL |
| stars | stars | INTEGER | NULL | GitHub stars count |
| forks | forks | INTEGER | NULL | GitHub forks count |
| watchers | watchers | INTEGER | NULL | GitHub watchers count |
| openIssues | open_issues | INTEGER | NULL | Open issues count |
| language | language | VARCHAR(50) | NULL | Primary programming language |
| lastPushed | last_pushed | TIMESTAMP | NULL | Last push timestamp |
| createdAt | created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updatedAt | updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| isMonitored | is_monitored | BOOLEAN | DEFAULT FALSE | Whether repository is monitored |
| dependencyCount | dependency_count | INTEGER | DEFAULT 0 | Number of dependencies |
| vulnerableDependencyCount | vulnerable_dependency_count | INTEGER | DEFAULT 0 | Number of vulnerable dependencies |
| repositoryRiskScore | repository_risk_score | DOUBLE PRECISION | NULL | Repository risk score |

**Unique Constraint**: `full_name`

### 5. Recommendation Entity → `recommendations` table

**Java Class**: `com.example.floggy.domain.Recommendation`
**Table Name**: `recommendations`

| Java Field | Column Name | Type | Constraints | Description |
|------------|-------------|------|-------------|-------------|
| id | id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| dependency | dependency_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to dependency |
| currentVersion | current_version | VARCHAR(100) | NOT NULL | Current version |
| recommendedVersion | recommended_version | VARCHAR(100) | NOT NULL | Recommended version |
| alternativeVersion | alternative_version | VARCHAR(100) | NULL | Alternative safe version |
| reasoning | reasoning | TEXT | NULL | Reasoning for recommendation |
| confidenceScore | confidence_score | DOUBLE PRECISION | NULL | Confidence score (0-100) |
| riskLevel | risk_level | VARCHAR(20) | CHECK constraint | Risk level: LOW, MEDIUM, HIGH |
| recommendationType | recommendation_type | VARCHAR(20) | CHECK constraint | Type: UPGRADE, HOLD, DOWNGRADE, ALTERNATIVE |
| estimatedSavingsHours | estimated_savings_hours | INTEGER | NULL | Estimated developer hours saved |
| hasBreakingChanges | has_breaking_changes | BOOLEAN | NULL | Whether version has breaking changes |
| hasSecurityFixes | has_security_fixes | BOOLEAN | NULL | Whether version has security fixes |
| hasPerformanceImprovements | has_performance_improvements | BOOLEAN | NULL | Whether version has performance improvements |
| hasBugFixes | has_bug_fixes | BOOLEAN | NULL | Whether version has bug fixes |
| generatedBy | generated_by | VARCHAR(20) | CHECK constraint | Generated by: AI, RULE_BASED, HYBRID |
| generatedAt | generated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Generation timestamp |
| expiresAt | expires_at | TIMESTAMP | NULL | Recommendation expiration |
| isApplied | is_applied | BOOLEAN | DEFAULT FALSE | Whether recommendation was applied |
| appliedAt | applied_at | TIMESTAMP | NULL | When recommendation was applied |
| createdAt | created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updatedAt | updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Foreign Key**: `dependency_id` references `dependencies(id)` ON DELETE CASCADE

### 6. RiskAssessment Entity → `risk_assessments` table

**Java Class**: `com.example.floggy.domain.RiskAssessment`
**Table Name**: `risk_assessments`

| Java Field | Column Name | Type | Constraints | Description |
|------------|-------------|------|-------------|-------------|
| id | id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| dependencyVersion | dependency_version_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to dependency version |
| assessmentType | assessment_type | VARCHAR(20) | NOT NULL, CHECK constraint | Type: SECURITY, STABILITY, PERFORMANCE, OVERALL |
| score | score | DOUBLE PRECISION | NULL | Assessment score (0-100) |
| level | level | VARCHAR(20) | CHECK constraint | Risk level: LOW, MEDIUM, HIGH, CRITICAL |
| factors | factors | TEXT | NULL | JSON or comma-separated risk factors |
| vulnerabilityCount | vulnerability_count | INTEGER | DEFAULT 0 | Number of vulnerabilities |
| rollbackCount | rollback_count | INTEGER | DEFAULT 0 | Number of rollbacks |
| hotfixCount | hotfix_count | INTEGER | DEFAULT 0 | Number of hotfixes |
| issueCount | issue_count | INTEGER | DEFAULT 0 | Number of open issues |
| pullRequestCount | pull_request_count | INTEGER | DEFAULT 0 | Number of open pull requests |
| communityHealthScore | community_health_score | DOUBLE PRECISION | NULL | Community health score |
| maintenanceScore | maintenance_score | DOUBLE PRECISION | NULL | Maintenance score |
| adoptionScore | adoption_score | DOUBLE PRECISION | NULL | Adoption score |
| aiExplanation | ai_explanation | TEXT | NULL | AI-generated explanation |
| assessedAt | assessed_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Assessment timestamp |
| createdAt | created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updatedAt | updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Foreign Key**: `dependency_version_id` references `dependency_versions(id)` ON DELETE CASCADE
**Unique Constraint**: `(dependency_version_id, assessment_type)`

## Entity Relationships

```
Dependency (1) ──┐ (many) DependencyVersion (1) ──┐ (many) Vulnerability
                 │                                │
                 └── (many) Recommendation        └── (many) RiskAssessment

GitHubRepository (standalone)
```

## Database Triggers

The schema includes automatic timestamp updates via PostgreSQL triggers:

1. **`update_updated_at_column()`** function updates the `updated_at` column to current timestamp
2. Triggers on all tables call this function before UPDATE operations

## Indexes Created

The schema creates the following indexes for performance optimization:

1. `idx_dependencies_package_manager` - Filter by package manager
2. `idx_dependencies_risk_level` - Filter by risk level
3. `idx_dependency_versions_dependency_id` - Join with dependencies
4. `idx_dependency_versions_recommendation` - Filter by recommendation status
5. `idx_vulnerabilities_dependency_version_id` - Join with dependency versions
6. `idx_vulnerabilities_severity` - Filter by severity
7. `idx_vulnerabilities_source` - Filter by source
8. `idx_github_repositories_full_name` - Lookup by repository name
9. `idx_github_repositories_is_monitored` - Filter monitored repositories
10. `idx_recommendations_dependency_id` - Join with dependencies
11. `idx_recommendations_is_applied` - Filter applied recommendations
12. `idx_risk_assessments_dependency_version_id` - Join with dependency versions
13. `idx_risk_assessments_assessment_type` - Filter by assessment type

## Usage Instructions

### 1. Initialize Database
```bash
# Using Docker Compose (from project root)
docker-compose up -d postgres

# Or manually create database
createdb -U postgres floggy
psql -U postgres -d floggy -f scripts/postgresql-schema.sql
```

### 2. Apply Schema
```sql
-- Connect to PostgreSQL
psql -U floggy -d floggy -h localhost

-- Execute schema script
\i scripts/postgresql-schema.sql
```

### 3. Verify Tables
```sql
-- List all tables
\dt

-- Describe a specific table
\d dependencies
```

## Notes

1. The schema uses `BIGSERIAL` for auto-incrementing IDs to support large datasets
2. All tables include `created_at` and `updated_at` timestamps for auditing
3. Foreign keys use `ON DELETE CASCADE` to maintain referential integrity
4. CHECK constraints enforce domain-specific values (enums from Java)
5. Text columns use appropriate length limits for performance
6. The schema is compatible with Spring Boot JPA `spring.jpa.hibernate.ddl-auto=update`