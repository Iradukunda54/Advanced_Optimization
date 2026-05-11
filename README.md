# Blogging Platform - Phase 3

A highly optimized Spring Boot application featuring Spring Data JPA integration, advanced query handling, transaction management, and caching.

## Technical Stack
- **Framework**: Spring Boot 3.2.4 (Spring Data JPA, Spring Cache, Validation, AOP)
- **Language**: Java 21
- **Database**: PostgreSQL
- **Documentation**: Springdoc OpenAPI (Swagger UI)
- **API**: REST & GraphQL

## New Phase 3 Features
- **Spring Data JPA Integration**: Simplified database access using repository abstraction.
- **Advanced Querying**:
    - Derived queries (e.g., `findByAuthorUsername`)
    - Custom JPQL for popular post retrieval.
    - Native SQL queries for complex join operations.
- **Pagination & Sorting**: Robust support for navigating large datasets in posts, comments, and reviews.
- **Transaction Management**: Guaranteed data consistency using `@Transactional` with appropriate propagation and rollback rules.
- **Spring Cache**: Significant read performance improvement using `@Cacheable` and `@CacheEvict` for frequently accessed data (posts, users, popular posts).
- **Performance Optimization**: Replaced manual sorting (QuickSort) with database-level sorting for better scalability.

## Setup Instructions

### 1. Database Configuration
Ensure PostgreSQL is running on `localhost:5432`.
The application expects a database named `Blogweb`.

Create your own profile-specific configuration file (e.g., `src/main/resources/application-dev.yml`) using the template below:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/Blogweb
    username: <YOUR_DB_USERNAME>
    password: <YOUR_DB_PASSWORD>
    driver-class-name: org.postgresql.Driver
```

> **Note:** The `application-dev.yml`, `application-test.yml`, and `application-prod.yml` files are excluded from version control via `.gitignore` to protect sensitive credentials.

### 2. Running the Application
Use Maven to run the application:
```bash
mvn spring-boot:run
```

### 3. API Documentation
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **GraphiQL**: [http://localhost:8080/graphiql](http://localhost:8080/graphiql)

## Testing New Features

### Pagination & Sorting
`GET /api/posts?page=0&size=5&sort=createdAt,desc`

### Popular Posts (Cached)
`GET /api/posts/popular?limit=5`

### Author's Posts
`GET /api/posts/author/{username}`

### Paginated Comments

## Phase 4: Security & Authentication
- **Spring Security**: Configured to protect all endpoints with custom access rules.
- **JWT Authentication**: Stateless authentication using signed JWT tokens.
- **Google OAuth2**: Integrated Google login for seamless user registration and authentication.
- **RBAC (Role-Based Access Control)**: Enforced permissions for `ADMIN`, `AUTHOR`, and `READER` roles.
- **Password Hashing**: Secure storage using BCrypt.
- **Security Monitoring**: Logging authentication events and request patterns using AOP.
- **DSA for Security**: In-memory token blacklisting for session revocation.

## Security Concepts: CORS vs. CSRF

### CORS (Cross-Origin Resource Sharing)
**Problem**: Browsers block cross-origin requests by default for security.
**Solution**: CORS allows the server to specify which origins are permitted to access its resources.
**Implementation**: In this project, CORS is configured in `SecurityConfig` to allow specific origins and methods, enabling frontend clients (React/JavaFX) to interact with the API.

### CSRF (Cross-Site Request Forgery)
**Problem**: An attacker tricks a user's browser into performing an unwanted action on a different website where the user is authenticated.
**Solution**: Synchronizer Token Pattern or disabling for stateless APIs.
**Implementation**: CSRF is **disabled** in this project because we use stateless JWT-based authentication. Since JWTs are typically sent in the `Authorization` header (not cookies), browsers do not automatically attach them to cross-origin requests, making the platform inherently resistant to CSRF.

## API Endpoints (Security)

### Authentication
- `POST /api/auth/register`: Register a new user.
- `POST /api/auth/login`: Login and receive a JWT.
- `GET /oauth2/authorization/google`: Initiate Google Login.

### RBAC Examples
- `POST /api/posts`: Requires `AUTHOR` or `ADMIN`.
- `DELETE /api/posts/{id}`: Requires `ADMIN`.
- `GET /api/users`: Requires `ADMIN`.

