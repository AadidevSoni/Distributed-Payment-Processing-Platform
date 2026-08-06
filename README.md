# Distributed Payment Processing Platform (Visa-Sim)

A miniature, production-quality simulation of a distributed payment processing backend — built as a hands-on learning project to master Spring Boot, Java 21, and the architectural patterns used in real fintech systems (Visa, Amazon, Google, Goldman Sachs-style engineering).

The goal is not maximum features — the goal is **maximum engineering quality**. Every design decision is deliberate and explainable, not copy-pasted.

---

## Project Goal

Build a simplified, distributed version of a Visa-like payment backend, composed of independent microservices communicating over REST and Kafka, backed by PostgreSQL and Redis, with production concerns (security, idempotency, retries, rate limiting, monitoring) treated as first-class citizens rather than afterthoughts.

### Target Architecture (Full Vision)

- **User Service** — user identity and profile management
- **Wallet Service** — balance management per user
- **Transaction Service** — payment initiation and processing
- **Fraud Detection Service** — real-time transaction risk scoring
- **Notification Service** — async user notifications
- **Ledger Service** — immutable financial record-keeping
- **API Gateway** — single entry point routing to all services

### Cross-Cutting Concerns

- PostgreSQL (persistence)
- Redis (caching, idempotency, distributed locking, rate limiting)
- Kafka (event-driven communication between services)
- Docker Compose (local orchestration)
- JWT Authentication + Spring Security
- JPA / Hibernate
- Unit + Integration Tests
- Idempotency guarantees on payment operations
- Retry queues / Dead Letter Queues
- Distributed locking
- Rate limiting
- Monitoring / observability

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Build Tool | Maven (with Maven Wrapper) |
| Persistence | PostgreSQL, Spring Data JPA (planned) |
| Caching / Locking | Redis (planned) |
| Messaging | Apache Kafka (planned) |
| Containerization | Docker Compose (planned) |
| Security | Spring Security, JWT (planned) |

---

## Architecture Principles

- **Layered / Clean Architecture** — dependencies flow inward; services never expose internal entities directly
- **DTOs at every boundary** — request/response shapes are decoupled from persistence models
- **Constructor injection only** — no field injection, for immutability and testability
- **Each service is independently named and structured** (`com.visasim.<service-name>`) to reinforce true microservice ownership, even while co-located in this practice monorepo
- **Conventional Commits** for all git history

---

## Repository Structure

```
payment-platform/
└── user-service/
    ├── pom.xml
    ├── .gitignore
    └── src/
        ├── main/
        │   ├── java/com/visasim/userservice/
        │   │   ├── controller/
        │   │   ├── service/
        │   │   ├── dto/
        │   │   ├── model/
        │   │   └── exception/
        │   └── resources/
        │       └── application.yml
        └── test/
```

Additional services (`wallet-service`, `transaction-service`, etc.) will be added as siblings of `user-service` as the project progresses through its milestones.

---

## Development Roadmap

| # | Milestone | Status |
|---|---|---|
| 1 | Spring Boot Project Setup | ✅ Done |
| 2 | REST API (Users) | ✅ Done |
| 3 | PostgreSQL Integration | ⬜ Not started |
| 4 | Wallets | ⬜ Not started |
| 5 | Transactions | ⬜ Not started |
| 6 | Concurrency | ⬜ Not started |
| 7 | Kafka | ⬜ Not started |
| 8 | Redis | ⬜ Not started |
| 9 | Fraud Detection | ⬜ Not started |
| 10 | Docker | ⬜ Not started |
| 11 | Microservices (multi-service split) | ⬜ Not started |
| 12 | Monitoring | ⬜ Not started |
| 13 | Performance Testing | ⬜ Not started |
| 14 | Deployment | ⬜ Not started |

---

## What's Implemented So Far

### Milestone 1 — Spring Boot Project Setup ✅
- Bootstrapped `user-service` on Spring Boot 3.3.4 / Java 21
- Maven Wrapper generated for reproducible builds
- Embedded Tomcat running on port `8081`
- Spring Boot Actuator wired in, `/actuator/health` verified working
- Established `com.visasim.userservice` package convention for future microservice independence

### Milestone 2 — REST API ✅
- `User` domain model (UUID-based identity, `Instant`-based UTC timestamps)
- Immutable request/response DTOs (`CreateUserRequest`, `UserResponse`) using Java records
- Bean Validation (`@NotBlank`, `@Email`) on incoming requests
- `UserController` exposing:
  - `POST /users` — create a user (returns `201 Created`)
  - `GET /users/{id}` — fetch a user by ID (returns `200 OK` or `404 Not Found`)
- `UserService` with an in-memory `ConcurrentHashMap` store (temporary placeholder — will be replaced by PostgreSQL + Spring Data JPA in Milestone 3)
- `GlobalExceptionHandler` (`@RestControllerAdvice`) providing consistent, clean error responses for validation failures and not-found errors
- Constructor-based dependency injection throughout (no field injection)

---

## Running the Project Locally

```bash
cd user-service
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw spring-boot:run
```

Verify it's alive:

```bash
curl http://localhost:8081/actuator/health
```

### Example: Create a User

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{"fullName": "Aadidev", "email": "aadidev@example.com"}'
```

### Example: Fetch a User

```bash
curl http://localhost:8081/users/{id}
```

---

## Requirements

- Java 21 (LTS)
- Maven (or use the included wrapper — no local install needed)

---

## License

This is a personal learning project and does not currently carry an open-source license.
