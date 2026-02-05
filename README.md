# Smart Dependency Advisor (Floggy)

## About the Project

**Smart Dependency Advisor** is an AI-powered system that helps developers make safer, data-driven dependency update decisions. It monitors dependency releases, vulnerabilities, and advisories in real time, analyzes GitHub signals, and uses Google Gemini 3 to reason over ecosystem-wide behavior to recommend stable versions instead of blindly suggesting the latest.

### Key Features

* **Real-time Monitoring**: Tracks dependency releases, vulnerabilities, and advisories
* **GitHub Signal Analysis**: Analyzes pull requests, rollbacks, hotfixes, and issue spikes
* **AI-Powered Reasoning**: Uses Google Gemini 3 for contextual analysis of dependency behavior
* **Risk Assessment**: Provides comprehensive risk scores for dependencies
* **Automated Recommendations**: Suggests stable versions with clear explanations
* **Project Scanning**: Automatically scans projects for dependency analysis
* **CI/CD Integration**: GitHub Actions integration for automated checks

## Architecture

The system is built with a modular, production-ready architecture:

* **Backend**: Java with Spring Boot for APIs, orchestration, and validation
* **AI Layer**: Google Gemini 3 for contextual reasoning over dependency behavior
* **Data Sources**: GitHub repositories, pull requests, vulnerability databases, and package registries
* **Automation**: Shell scripts and GitHub APIs for dependency scanning and PR creation
* **Frontend**: React for a clear and accessible developer interface (planned)
* **Infrastructure**: Docker for reproducibility and CI compatibility

## Quick Start

### Prerequisites

* Java 21+
* Maven 3.9+
* Docker and Docker Compose (optional)
* PostgreSQL (optional, embedded H2 available)

### Running with Docker Compose

The easiest way to run the complete system:

```bash
# Clone the repository
git clone <repository-url>
cd Floggy

# Set up environment variables (optional)
export GITHUB_TOKEN=your_github_token
export GEMINI_API_KEY=your_gemini_api_key

# Start the services
docker-compose up -d
```

The application will be available at `http://localhost:8080`

### Running Locally

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/*.jar
```

## API Documentation

### Health Check
```
GET /api/v1/dependencies/health
```

### Analyze a Dependency
```
POST /api/v1/dependencies/analyze
Parameters:
  packageManager: npm, maven, gradle, pip, etc.
  dependency: Dependency name
  currentVersion: Current version
  targetVersion: Target version to analyze
```

### Get Risk Assessment
```
GET /api/v1/dependencies/risk-assessment
Parameters:
  packageManager: Package manager
  dependency: Dependency name
  version: Version to assess
```

### Batch Analyze
```
POST /api/v1/dependencies/batch-analyze
Body: Map of dependencies with target versions
```

### Get Vulnerabilities
```
GET /api/v1/dependencies/vulnerabilities
Parameters:
  packageManager: Package manager
  dependency: Dependency name
  version: Version (optional)
```

## Automation Scripts

### Project Dependency Scanner

Scan your project for dependencies and get analysis:

```bash
# Make the script executable
chmod +x scripts/scan-dependencies.sh

# Run the scanner
./scripts/scan-dependencies.sh --project-dir /path/to/your/project --verbose
```

### GitHub Action Integration

Use the provided GitHub Action script in your workflows:

```yaml
name: Dependency Security Scan
on: [push, pull_request]
jobs:
  dependency-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Smart Dependency Advisor
        run: |
          chmod +x scripts/github-action.sh
          ./scripts/github-action.sh --api-url ${{ secrets.API_URL }} --fail-on-critical
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `GITHUB_TOKEN` | GitHub API token for accessing repositories | (none) |
| `GEMINI_API_KEY` | Google Gemini API key | (none) |
| `SNYK_API_TOKEN` | Snyk API token for vulnerability data | (none) |
| `SPRING_DATASOURCE_URL` | Database connection URL | jdbc:postgresql://localhost:5432/floggy |
| `SPRING_DATASOURCE_USERNAME` | Database username | floggy |
| `SPRING_DATASOURCE_PASSWORD` | Database password | floggy123 |

### Application Properties

Key configuration in `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/floggy
spring.jpa.hibernate.ddl-auto=update

# External APIs
github.token=${GITHUB_TOKEN:}
gemini.api.key=${GEMINI_API_KEY:}
vulnerability.snyk.api.token=${SNYK_API_TOKEN:}
```

## Project Structure

```
Floggy/
├── src/main/java/com/example/floggy/
│   ├── controller/          # REST API controllers
│   ├── service/            # Business logic services
│   ├── domain/             # JPA entities
│   ├── config/             # Configuration classes
│   └── client/             # External API clients
├── src/main/resources/
│   ├── application.properties
│   └── graphql-client/     # GraphQL schemas
├── scripts/                # Automation scripts
│   ├── scan-dependencies.sh
│   └── github-action.sh
├── compose.yaml           # Docker Compose configuration
├── Dockerfile            # Docker build file
└── pom.xml              # Maven dependencies
```

## Development

### Building the Project

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Code Style

The project uses:
* Lombok for reducing boilerplate code
* Spring Boot conventions
* JPA for data persistence
* RESTful API design principles

## Security Considerations

1. **API Keys**: Store API keys in environment variables, not in code
2. **Database**: Use strong passwords for production databases
3. **Rate Limiting**: Implement rate limiting for external API calls
4. **Input Validation**: Validate all user inputs to prevent injection attacks

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

* Inspired by real-world challenges in enterprise dependency management
* Built during a hackathon to address production dependency issues
* Uses Google Gemini 3 for AI-powered analysis
* Integrates with GitHub, NVD, and other security databases