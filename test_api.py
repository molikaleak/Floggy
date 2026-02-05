#!/usr/bin/env python3
"""
Test script for Floggy API endpoints
"""

import requests
import json
import sys
from requests.auth import HTTPBasicAuth

# Configuration
BASE_URL = "http://localhost:8080/api/v1/dependencies"

# Try different credentials - Spring Security generates different passwords
CREDENTIALS = [
    ("user", "3393db18-7716-4637-81b1-ff9dac8b1a1c"),  # From earlier logs
    ("user", "fa73a347-8c51-413c-b2c6-88de509b32fd"),  # From test logs
    ("admin", "admin"),  # Common default
    ("floggy", "floggy123"),  # From database credentials
]

def get_auth():
    """Try to find working credentials"""
    for username, password in CREDENTIALS:
        print(f"Trying credentials: {username}:{password[:8]}...")
        try:
            response = requests.get(f"{BASE_URL}/health", auth=HTTPBasicAuth(username, password), timeout=2)
            if response.status_code == 200:
                print(f"✓ Found working credentials: {username}:{password[:8]}...")
                return HTTPBasicAuth(username, password)
        except:
            continue
    print("✗ No working credentials found")
    return None

def test_health(auth):
    """Test health endpoint"""
    print("Testing health endpoint...")
    if not auth:
        return False
    try:
        response = requests.get(f"{BASE_URL}/health", auth=auth)
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            print(f"Response: {response.json()}")
            return True
        else:
            print(f"Error: {response.text}")
            return False
    except Exception as e:
        print(f"Exception: {e}")
        return False

def test_vulnerabilities(auth):
    """Test vulnerabilities endpoint"""
    print("\nTesting vulnerabilities endpoint...")
    if not auth:
        return False
    params = {
        "packageManager": "maven",
        "dependency": "log4j",
        "version": "1.2.17"
    }
    try:
        response = requests.get(f"{BASE_URL}/vulnerabilities", 
                               params=params,
                               auth=auth)
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"Found {data.get('count', 0)} vulnerabilities for {data.get('dependency')}")
            print(f"Security score: {data.get('security_score')}")
            return True
        else:
            print(f"Error: {response.text}")
            return False
    except Exception as e:
        print(f"Exception: {e}")
        return False

def test_risk_assessment(auth):
    """Test risk assessment endpoint"""
    print("\nTesting risk assessment endpoint...")
    if not auth:
        return False
    params = {
        "packageManager": "maven",
        "dependency": "log4j",
        "version": "1.2.17"
    }
    try:
        response = requests.get(f"{BASE_URL}/risk-assessment", 
                               params=params,
                               auth=auth)
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"Risk assessment: {data}")
            return True
        else:
            print(f"Error: {response.text}")
            return False
    except Exception as e:
        print(f"Exception: {e}")
        return False

def test_analyze(auth):
    """Test analyze endpoint"""
    print("\nTesting analyze endpoint...")
    if not auth:
        return False
    params = {
        "packageManager": "maven",
        "dependency": "log4j",
        "currentVersion": "1.2.17",
        "targetVersion": "2.17.1"
    }
    try:
        response = requests.post(f"{BASE_URL}/analyze", 
                                params=params,
                                auth=auth)
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"Recommendation: {data}")
            return True
        else:
            print(f"Error: {response.text}")
            return False
    except Exception as e:
        print(f"Exception: {e}")
        return False

def test_github_info(auth):
    """Test GitHub info endpoint"""
    print("\nTesting GitHub info endpoint...")
    if not auth:
        return False
    params = {
        "dependency": "log4j"
    }
    try:
        response = requests.get(f"{BASE_URL}/github-info", 
                               params=params,
                               auth=auth)
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"GitHub info: Found {data.get('dependent_repo_count', 0)} repositories")
            return True
        else:
            print(f"Error: {response.text}")
            return False
    except Exception as e:
        print(f"Exception: {e}")
        return False

def test_simulate_project(auth):
    """Test simulate project endpoint"""
    print("\nTesting simulate project endpoint...")
    if not auth:
        return False
    dependencies = [
        "maven:log4j:1.2.17",
        "maven:spring-core:5.3.23",
        "npm:lodash:4.17.21"
    ]
    try:
        response = requests.post(f"{BASE_URL}/simulate-project", 
                                json=dependencies,
                                auth=auth)
        print(f"Status: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"Project analysis: {data.get('dependencies_analyzed')} dependencies")
            print(f"Total vulnerabilities: {data.get('total_vulnerabilities')}")
            print(f"Overall risk: {data.get('overall_project_risk')}")
            return True
        else:
            print(f"Error: {response.text}")
            return False
    except Exception as e:
        print(f"Exception: {e}")
        return False

def main():
    print("=" * 60)
    print("Floggy API Test Suite")
    print("=" * 60)
    
    # First, find working credentials
    auth = get_auth()
    if not auth:
        print("\nCannot proceed without authentication.")
        print("\nPossible solutions:")
        print("1. Check the Spring Boot logs for the generated password")
        print("2. Disable security in application.properties")
        print("3. Configure proper security credentials")
        return 1
    
    # Test all endpoints
    results = []
    
    results.append(("Health Check", test_health(auth)))
    results.append(("Vulnerabilities", test_vulnerabilities(auth)))
    results.append(("Risk Assessment", test_risk_assessment(auth)))
    results.append(("Analyze", test_analyze(auth)))
    results.append(("GitHub Info", test_github_info(auth)))
    results.append(("Simulate Project", test_simulate_project(auth)))
    
    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)
    
    passed = 0
    failed = 0
    
    for test_name, success in results:
        status = "✓ PASS" if success else "✗ FAIL"
        print(f"{test_name:30} {status}")
        if success:
            passed += 1
        else:
            failed += 1
    
    print(f"\nTotal: {len(results)} tests, {passed} passed, {failed} failed")
    
    if failed > 0:
        print("\nNote: Some tests may fail due to:")
        print("1. External API dependencies (GitHub, NVD, Snyk)")
        print("2. Database connectivity issues")
        print("3. Missing configuration (API keys)")
        print("4. External services not available")
    
    return 0 if failed == 0 else 1

if __name__ == "__main__":
    sys.exit(main())