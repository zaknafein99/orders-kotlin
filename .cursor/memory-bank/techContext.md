# Technical Context

## Technology Stack

### Backend
- Language: Kotlin 1.9.24
- Framework: Spring Boot 3.2.5
- Database: PostgreSQL
- ORM: Spring Data JPA
- Security: Spring Security with JWT
- API Documentation: SpringDoc OpenAPI 2.4.0
- Mapping: MapStruct 1.5.5.Final
- Logging: Kotlin Logging 3.0.5

### Frontend
- Framework: (To be determined)
- State Management: (To be determined)
- UI Components: (To be determined)
- API Client: (To be determined)

### Infrastructure
- Containerization: Docker
- CI/CD: GitHub Actions
- Version Control: Git
- IDE: VS Code with Dev Containers

## Development Setup

### Prerequisites
1. JDK 21
2. Docker and Docker Compose
3. Git
4. VS Code with extensions:
   - Kotlin Language
   - Spring Boot Extension Pack
   - Docker
   - Dev Containers

### Local Development
1. Clone repository
2. Start PostgreSQL container
3. Run application using Gradle
4. Access API at http://localhost:8080
5. Access Swagger UI at http://localhost:8080/swagger-ui.html

### Docker Development
1. Build image: `./gradlew dockerBuild`
2. Run container: `./gradlew dockerRun`
3. Access application at http://localhost:8080

## Database Schema
- Customers
- Orders
- Items
- Trucks
- Users
- Order Items (Join table)

## API Structure
- RESTful endpoints
- JWT authentication
- Role-based access
- Pagination support
- Error handling
- Validation

## Security
- JWT token-based authentication
- Role-based authorization
- Password encryption
- Token refresh mechanism
- CORS configuration

## Testing
- Unit tests with JUnit
- Integration tests
- Security tests
- API tests

## Deployment
- Docker containerization
- Environment configuration
- Database migration
- Security settings
- Logging configuration 