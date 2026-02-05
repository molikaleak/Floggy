#!/bin/bash

# Smart Dependency Advisor - Dependency Scanning Script
# This script scans project dependencies and sends them to the Smart Dependency Advisor API

set -e

# Configuration
API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api/v1/dependencies}"
PROJECT_DIR="${PROJECT_DIR:-.}"
OUTPUT_FILE="${OUTPUT_FILE:-dependency-report.json}"
VERBOSE="${VERBOSE:-false}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if jq is installed
check_dependencies() {
    if ! command -v jq &> /dev/null; then
        log_error "jq is not installed. Please install jq to use this script."
        exit 1
    fi
    
    if ! command -v curl &> /dev/null; then
        log_error "curl is not installed. Please install curl to use this script."
        exit 1
    fi
}

# Scan Maven dependencies
scan_maven() {
    local pom_file="$1"
    log_info "Scanning Maven dependencies from $pom_file"
    
    if [ ! -f "$pom_file" ]; then
        log_warning "POM file not found: $pom_file"
        return
    fi
    
    # Extract dependencies using maven dependency:list (simplified approach)
    # In a real implementation, you would parse the POM file or use mvn dependency:list
    log_info "Maven scanning would be implemented here"
    echo "[]"
}

# Scan npm dependencies
scan_npm() {
    local package_file="$1"
    log_info "Scanning npm dependencies from $package_file"
    
    if [ ! -f "$package_file" ]; then
        log_warning "package.json not found: $package_file"
        return
    fi
    
    # Extract dependencies from package.json
    if command -v jq &> /dev/null; then
        jq -r '.dependencies // {}, .devDependencies // {} | to_entries[] | "npm:\(.key):\(.value)"' "$package_file"
    else
        log_warning "jq not available, skipping npm scanning"
        echo "[]"
    fi
}

# Scan Gradle dependencies
scan_gradle() {
    local gradle_file="$1"
    log_info "Scanning Gradle dependencies from $gradle_file"
    
    if [ ! -f "$gradle_file" ]; then
        log_warning "Gradle file not found: $gradle_file"
        return
    fi
    
    log_info "Gradle scanning would be implemented here"
    echo "[]"
}

# Scan Python dependencies
scan_python() {
    local requirements_file="$1"
    log_info "Scanning Python dependencies from $requirements_file"
    
    if [ ! -f "$requirements_file" ]; then
        log_warning "Requirements file not found: $requirements_file"
        return
    fi
    
    # Extract dependencies from requirements.txt
    grep -E '^[a-zA-Z0-9_-]+' "$requirements_file" | while read -r line; do
        # Parse package and version
        package=$(echo "$line" | cut -d'=' -f1 | cut -d'>' -f1 | cut -d'<' -f1 | cut -d'~' -f1 | xargs)
        version=$(echo "$line" | grep -oE '[=<>!~]=?[0-9.*a-zA-Z_-]+' | head -1 || echo "latest")
        echo "pip:$package:$version"
    done
}

# Send dependencies to API for analysis
analyze_dependencies() {
    local dependencies_file="$1"
    
    if [ ! -s "$dependencies_file" ]; then
        log_warning "No dependencies found to analyze"
        return
    fi
    
    log_info "Sending dependencies to Smart Dependency Advisor API..."
    
    # Create a JSON array from the dependencies
    local json_array="["
    local first=true
    
    while IFS= read -r dep; do
        if [ -n "$dep" ]; then
            if [ "$first" = true ]; then
                first=false
            else
                json_array="$json_array,"
            fi
            
            # Parse dependency string (format: packageManager:name:version)
            IFS=':' read -r package_manager name version <<< "$dep"
            
            json_array="$json_array{\"packageManager\":\"$package_manager\",\"name\":\"$name\",\"version\":\"$version\"}"
        fi
    done < "$dependencies_file"
    
    json_array="$json_array]"
    
    if [ "$VERBOSE" = "true" ]; then
        log_info "Request payload: $json_array"
    fi
    
    # Send to API
    local response=$(curl -s -X POST "${API_BASE_URL}/batch-analyze" \
        -H "Content-Type: application/json" \
        -d "$json_array" \
        --fail-with-body 2>/dev/null || true)
    
    if [ -n "$response" ]; then
        echo "$response" | jq '.' > "$OUTPUT_FILE"
        log_success "Analysis complete! Results saved to $OUTPUT_FILE"
        
        # Print summary
        local total=$(echo "$response" | jq 'length')
        local critical=$(echo "$response" | jq '[.[] | select(.riskLevel == "CRITICAL")] | length')
        local high=$(echo "$response" | jq '[.[] | select(.riskLevel == "HIGH")] | length')
        
        log_info "Summary:"
        log_info "  Total dependencies analyzed: $total"
        if [ "$critical" -gt 0 ]; then
            log_error "  CRITICAL risk: $critical"
        fi
        if [ "$high" -gt 0 ]; then
            log_warning "  HIGH risk: $high"
        fi
    else
        log_error "Failed to analyze dependencies. Is the API server running?"
    fi
}

# Main function
main() {
    log_info "Starting Smart Dependency Advisor Scanner"
    log_info "Project directory: $PROJECT_DIR"
    log_info "API base URL: $API_BASE_URL"
    
    check_dependencies
    
    # Create temporary file for dependencies
    local deps_file=$(mktemp)
    
    # Scan for different package managers
    log_info "Scanning project for dependencies..."
    
    # Maven
    if [ -f "$PROJECT_DIR/pom.xml" ]; then
        scan_maven "$PROJECT_DIR/pom.xml" >> "$deps_file"
    fi
    
    # npm
    if [ -f "$PROJECT_DIR/package.json" ]; then
        scan_npm "$PROJECT_DIR/package.json" >> "$deps_file"
    fi
    
    # Gradle
    if [ -f "$PROJECT_DIR/build.gradle" ] || [ -f "$PROJECT_DIR/build.gradle.kts" ]; then
        if [ -f "$PROJECT_DIR/build.gradle" ]; then
            scan_gradle "$PROJECT_DIR/build.gradle" >> "$deps_file"
        fi
        if [ -f "$PROJECT_DIR/build.gradle.kts" ]; then
            scan_gradle "$PROJECT_DIR/build.gradle.kts" >> "$deps_file"
        fi
    fi
    
    # Python
    if [ -f "$PROJECT_DIR/requirements.txt" ]; then
        scan_python "$PROJECT_DIR/requirements.txt" >> "$deps_file"
    fi
    if [ -f "$PROJECT_DIR/pyproject.toml" ]; then
        log_info "pyproject.toml detected (Python project)"
        # In a real implementation, parse pyproject.toml
    fi
    
    # Count dependencies found
    local dep_count=$(grep -c . "$deps_file" || echo 0)
    
    if [ "$dep_count" -eq 0 ]; then
        log_warning "No dependencies found in project"
        rm "$deps_file"
        exit 0
    fi
    
    log_info "Found $dep_count dependencies"
    
    if [ "$VERBOSE" = "true" ]; then
        log_info "Dependencies found:"
        cat "$deps_file"
    fi
    
    # Analyze dependencies
    analyze_dependencies "$deps_file"
    
    # Cleanup
    rm "$deps_file"
    
    log_success "Scan completed successfully!"
}

# Handle script arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --api-url)
            API_BASE_URL="$2"
            shift 2
            ;;
        --project-dir)
            PROJECT_DIR="$2"
            shift 2
            ;;
        --output)
            OUTPUT_FILE="$2"
            shift 2
            ;;
        --verbose)
            VERBOSE="true"
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --api-url URL     Base URL of the Smart Dependency Advisor API"
            echo "  --project-dir DIR Project directory to scan (default: .)"
            echo "  --output FILE     Output file for results (default: dependency-report.json)"
            echo "  --verbose         Enable verbose output"
            echo "  --help            Show this help message"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Run main function
main