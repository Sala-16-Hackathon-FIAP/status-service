# status-service

Tracks the lifecycle of each video upload and processing job. Consumes all four RabbitMQ events and exposes endpoints so users can query current processing status.

## Technology Stack

- **Java 21** + **Spring Boot 3.5.0**
- **Spring Security** with JWT (JJWT 0.12.6)
- **PostgreSQL 16** with **Flyway** migrations
- **RabbitMQ** via `rabbit-topic-lib` (choreographed saga)
- **New Relic** APM (Java Agent v8.15.0)
- **SpringDoc OpenAPI** (Swagger UI)
- **JaCoCo** for code coverage (minimum 80%)
- **Hexagonal Architecture** (ports and adapters)

## Responsibility

- Consume all processing lifecycle events from RabbitMQ
- Maintain a `JobStatus` record per upload, updating its status as events arrive
- Expose HTTP endpoints for users to query status by upload ID or list all their statuses

## Architecture

Hexagonal (ports and adapters):

```
infrastructure/rest        -> HTTP layer (StatusController)
application/port/input     -> StatusUseCase interface
application/service        -> StatusService (use-case implementation)
infrastructure/persistence -> Spring Data JPA + Flyway
infrastructure/messaging   -> RabbitMQ consumers (rabbit-topic-lib)
infrastructure/security    -> JwtAuthFilter, SecurityConfig
infrastructure/monitoring  -> NewRelicTracker
```

## API Endpoints

All endpoints require `Authorization: Bearer <JWT>`.

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/status` | List all job statuses for the authenticated user |
| `GET` | `/api/v1/status/uploads/{uploadId}` | Get status for a specific upload |

### List all statuses

```bash
curl -s http://localhost:8084/api/v1/status \
  -H "Authorization: Bearer $TOKEN" | jq
```

Response:
```json
[
  {
    "uploadId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "jobId": "...",
    "filename": "video.mp4",
    "status": "PROCESSING_COMPLETED",
    "resultS3Key": "processed/<userId>/<jobId>/frames.zip",
    "errorMessage": null,
    "updatedAt": "2025-05-28T10:00:42"
  }
]
```

### Get status by upload ID

```bash
curl -s "http://localhost:8084/api/v1/status/uploads/$UPLOAD_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Poll until complete

```bash
while true; do
  STATUS=$(curl -s "http://localhost:8084/api/v1/status/uploads/$UPLOAD_ID" \
    -H "Authorization: Bearer $TOKEN" | jq -r '.status')
  echo "Status: $STATUS"
  [[ "$STATUS" == "PROCESSING_COMPLETED" || "$STATUS" == "PROCESSING_FAILED" ]] && break
  sleep 5
done
```

### Swagger UI

http://localhost:8084/swagger-ui.html

## Status State Machine

```
UPLOAD_COMPLETED
      |
      v (video.processing.started)
PROCESSING_STARTED
      |
      |---> (video.processing.completed) ---> PROCESSING_COMPLETED
      |
      \---> (video.processing.failed)    ---> PROCESSING_FAILED
```

## RabbitMQ Events

**Exchange:** `fiapx.events` (topic)

| Direction | Routing key | Queue |
|---|---|---|
| **Consumes** | `video.upload.completed` | `status.video.upload.completed` |
| **Consumes** | `video.processing.started` | `status.video.processing.started` |
| **Consumes** | `video.processing.completed` | `status.video.processing.completed` |
| **Consumes** | `video.processing.failed` | `status.video.processing.failed` |

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5436/fiapx_status` | JDBC connection URL |
| `DB_USERNAME` | `fiapx` | Database user |
| `DB_PASSWORD` | `fiapx123` | Database password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `RABBITMQ_USER` | `fiapx` | RabbitMQ user |
| `RABBITMQ_PASS` | `fiapx123` | RabbitMQ password |
| `JWT_SECRET` | *(dev key)* | Must match auth-service |

## Running Locally

### 1. Start infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL (port 5436) and RabbitMQ (port 5672 / management 15672).

### 2. Run the application

Run from your IDE or with Maven:

```bash
mvn spring-boot:run
```

The application starts on **port 8084**. All defaults in `application.yml` point to `localhost`.

## Tests

```bash
mvn test
```

JaCoCo enforces **>= 80% instruction coverage**. Coverage report: `target/site/jacoco/index.html`.

## CI/CD

GitHub Actions workflow: build -> test -> SonarCloud -> GHCR push -> EKS deploy.

The `GITHUB_TOKEN` secret is required in CI to download `rabbit-topic-lib` from GitHub Packages. Docker image is **public** on GHCR.

## Database

- PostgreSQL 16, schema: `fiapx_status`
- Migrations managed by Flyway (`src/main/resources/db/migration`)
