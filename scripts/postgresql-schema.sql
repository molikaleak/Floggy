-- PostgreSQL Schema for Floggy Dependency Analysis System
-- Generated from Java Entity Classes
-- Database: floggy
-- User: floggy

-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS risk_assessments CASCADE;
DROP TABLE IF EXISTS recommendations CASCADE;
DROP TABLE IF EXISTS vulnerabilities CASCADE;
DROP TABLE IF EXISTS dependency_versions CASCADE;
DROP TABLE IF EXISTS dependencies CASCADE;
DROP TABLE IF EXISTS github_repositories CASCADE;

-- Create dependencies table
CREATE TABLE dependencies (
    id BIGSERIAL PRIMARY KEY,
    package_manager VARCHAR(50) NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    artifact_id VARCHAR(255) NOT NULL,
    description TEXT,
    homepage VARCHAR(500),
    repository_url VARCHAR(500),
    total_downloads INTEGER,
    last_updated TIMESTAMP,
    risk_score DOUBLE PRECISION,
    risk_level VARCHAR(20) CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_dependency UNIQUE (package_manager, group_id, artifact_id)
);

-- Create dependency_versions table
CREATE TABLE dependency_versions (
    id BIGSERIAL PRIMARY KEY,
    dependency_id BIGINT NOT NULL,
    version VARCHAR(100) NOT NULL,
    release_date TIMESTAMP,
    is_latest BOOLEAN,
    is_stable BOOLEAN,
    download_count INTEGER,
    release_notes TEXT,
    github_stars INTEGER,
    github_forks INTEGER,
    github_issues INTEGER,
    github_pull_requests INTEGER,
    rollback_count INTEGER,
    hotfix_count INTEGER,
    stability_score DOUBLE PRECISION,
    security_score DOUBLE PRECISION,
    performance_score DOUBLE PRECISION,
    overall_score DOUBLE PRECISION,
    recommendation VARCHAR(20) CHECK (recommendation IN ('RECOMMENDED', 'CAUTION', 'AVOID')),
    analyzed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dependency_version_dependency FOREIGN KEY (dependency_id) 
        REFERENCES dependencies(id) ON DELETE CASCADE,
    CONSTRAINT unique_dependency_version UNIQUE (dependency_id, version)
);

-- Create vulnerabilities table
CREATE TABLE vulnerabilities (
    id BIGSERIAL PRIMARY KEY,
    dependency_version_id BIGINT,
    source VARCHAR(50) NOT NULL,
    vulnerability_id VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    cvss_score DOUBLE PRECISION,
    description TEXT,
    affected_versions VARCHAR(500),
    patched_versions VARCHAR(500),
    published_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    reference_urls TEXT,
    is_exploited BOOLEAN,
    has_fix BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vulnerability_dependency_version FOREIGN KEY (dependency_version_id) 
        REFERENCES dependency_versions(id) ON DELETE CASCADE,
    CONSTRAINT unique_vulnerability UNIQUE (source, vulnerability_id, dependency_version_id)
);

-- Create github_repositories table
CREATE TABLE github_repositories (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL UNIQUE,
    owner VARCHAR(100),
    name VARCHAR(100),
    description TEXT,
    url VARCHAR(500),
    stars INTEGER,
    forks INTEGER,
    watchers INTEGER,
    open_issues INTEGER,
    language VARCHAR(50),
    last_pushed TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_monitored BOOLEAN DEFAULT FALSE,
    dependency_count INTEGER DEFAULT 0,
    vulnerable_dependency_count INTEGER DEFAULT 0,
    repository_risk_score DOUBLE PRECISION
);

-- Create recommendations table
CREATE TABLE recommendations (
    id BIGSERIAL PRIMARY KEY,
    dependency_id BIGINT NOT NULL,
    current_version VARCHAR(100) NOT NULL,
    recommended_version VARCHAR(100) NOT NULL,
    alternative_version VARCHAR(100),
    reasoning TEXT,
    confidence_score DOUBLE PRECISION,
    risk_level VARCHAR(20) CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    recommendation_type VARCHAR(20) CHECK (recommendation_type IN ('UPGRADE', 'HOLD', 'DOWNGRADE', 'ALTERNATIVE')),
    estimated_savings_hours INTEGER,
    has_breaking_changes BOOLEAN,
    has_security_fixes BOOLEAN,
    has_performance_improvements BOOLEAN,
    has_bug_fixes BOOLEAN,
    generated_by VARCHAR(20) CHECK (generated_by IN ('AI', 'RULE_BASED', 'HYBRID')),
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_applied BOOLEAN DEFAULT FALSE,
    applied_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommendation_dependency FOREIGN KEY (dependency_id) 
        REFERENCES dependencies(id) ON DELETE CASCADE
);

-- Create risk_assessments table
CREATE TABLE risk_assessments (
    id BIGSERIAL PRIMARY KEY,
    dependency_version_id BIGINT NOT NULL,
    assessment_type VARCHAR(20) NOT NULL CHECK (assessment_type IN ('SECURITY', 'STABILITY', 'PERFORMANCE', 'OVERALL')),
    score DOUBLE PRECISION,
    level VARCHAR(20) CHECK (level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    factors TEXT,
    vulnerability_count INTEGER DEFAULT 0,
    rollback_count INTEGER DEFAULT 0,
    hotfix_count INTEGER DEFAULT 0,
    issue_count INTEGER DEFAULT 0,
    pull_request_count INTEGER DEFAULT 0,
    community_health_score DOUBLE PRECISION,
    maintenance_score DOUBLE PRECISION,
    adoption_score DOUBLE PRECISION,
    ai_explanation TEXT,
    assessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_risk_assessment_dependency_version FOREIGN KEY (dependency_version_id) 
        REFERENCES dependency_versions(id) ON DELETE CASCADE,
    CONSTRAINT unique_risk_assessment UNIQUE (dependency_version_id, assessment_type)
);

-- Create indexes for better query performance
CREATE INDEX idx_dependencies_package_manager ON dependencies(package_manager);
CREATE INDEX idx_dependencies_risk_level ON dependencies(risk_level);
CREATE INDEX idx_dependency_versions_dependency_id ON dependency_versions(dependency_id);
CREATE INDEX idx_dependency_versions_recommendation ON dependency_versions(recommendation);
CREATE INDEX idx_vulnerabilities_dependency_version_id ON vulnerabilities(dependency_version_id);
CREATE INDEX idx_vulnerabilities_severity ON vulnerabilities(severity);
CREATE INDEX idx_vulnerabilities_source ON vulnerabilities(source);
CREATE INDEX idx_github_repositories_full_name ON github_repositories(full_name);
CREATE INDEX idx_github_repositories_is_monitored ON github_repositories(is_monitored);
CREATE INDEX idx_recommendations_dependency_id ON recommendations(dependency_id);
CREATE INDEX idx_recommendations_is_applied ON recommendations(is_applied);
CREATE INDEX idx_risk_assessments_dependency_version_id ON risk_assessments(dependency_version_id);
CREATE INDEX idx_risk_assessments_assessment_type ON risk_assessments(assessment_type);

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for each table to automatically update updated_at
CREATE TRIGGER update_dependencies_updated_at BEFORE UPDATE ON dependencies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_dependency_versions_updated_at BEFORE UPDATE ON dependency_versions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vulnerabilities_updated_at BEFORE UPDATE ON vulnerabilities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_github_repositories_updated_at BEFORE UPDATE ON github_repositories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_recommendations_updated_at BEFORE UPDATE ON recommendations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_risk_assessments_updated_at BEFORE UPDATE ON risk_assessments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data (optional)
-- INSERT INTO dependencies (package_manager, group_id, artifact_id, description, risk_level) 
-- VALUES ('npm', '', 'react', 'React JavaScript library', 'LOW');

-- Grant permissions (adjust as needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO floggy;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO floggy;

COMMENT ON TABLE dependencies IS 'Stores information about software dependencies across different package managers';
COMMENT ON TABLE dependency_versions IS 'Stores version-specific information for each dependency';
COMMENT ON TABLE vulnerabilities IS 'Stores vulnerability information for dependency versions';
COMMENT ON TABLE github_repositories IS 'Stores GitHub repository information for monitoring';
COMMENT ON TABLE recommendations IS 'Stores AI-generated recommendations for dependency upgrades';
COMMENT ON TABLE risk_assessments IS 'Stores risk assessment scores for dependency versions across different dimensions';