# Mercadona POC - HMI Oven File Upload System

A Spring Boot application for managing and uploading recipe files to HMI ovens.

## Features

- Asynchronous file upload to multiple ovens
- Real-time progress updates via Server-Sent Events (SSE)
- File verification with SHA-256 checksums
- Automatic retry mechanism for failed uploads
- REST API for oven management
- H2 database for oven configuration
- Web scraping verification of uploaded files

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Network access to HMI ovens

## Installation

1. Clone the repository and configure your environment
2. Copy `src/main/resources/application.yml.example` to `application.yml`
3. Configure your credentials using environment variables (recommended)
4. Build: `./mvnw clean install`
5. Run: `./mvnw spring-boot:run`

## Async Configuration

The application uses a custom thread pool executor configured in `AsyncConfiguration.java`:

- **Core Pool Size**: 5 threads
- **Max Pool Size**: 20 threads
- **Queue Capacity**: 100 tasks
- **Keep Alive**: 60 seconds
- **Thread Prefix**: `async-executor-`

This provides optimal performance for concurrent file uploads to multiple ovens.

## API Endpoints

- `POST /api/sendFile` - Upload files to ovens (async)
- `GET /api/ovens/list` - Get paginated list of ovens
- `GET /api/ovens/{id}` - Get oven by ID
- `POST /api/ovens` - Create new oven
- `GET /api/recipes/list` - Get available recipe folders
- `GET /api/sse/emitter` - Subscribe to real-time updates

## Security Best Practices

1. Use environment variables for credentials (never commit them)
2. Use `application.yml.example` as template
3. Enable HTTPS in production
4. Consider adding Spring Security

## Testing

```bash
./mvnw test
./mvnw jacoco:report
```

## Code Quality Improvements Applied

- Custom async executor with proper thread pool sizing
- Constructor injection instead of @Autowired
- Global exception handler with @RestControllerAdvice
- Proper error handling for async operations
- Security: credentials moved to environment variables
- Enhanced .gitignore for sensitive files
