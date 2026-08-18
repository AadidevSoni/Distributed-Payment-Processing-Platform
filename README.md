# Distributed Payment Processing Platform (Visa-Sim)

A miniature, production-quality simulation of a distributed payment processing backend — built as a hands-on learning project to master Spring Boot, Java 21, and the architectural patterns used in real fintech systems (Visa, Amazon, Google, Goldman Sachs-style engineering).

The goal is not maximum features — the goal is **maximum engineering quality**. Every design decision is deliberate and explainable, not copy-pasted.

---

## Project Goal

Build a simplified, distributed version of a Visa-like payment backend, composed of independent microservices communicating over REST and Kafka, backed by PostgreSQL and Redis, with production concerns (security, idempotency, retries, rate limiting, fraud detection, monitoring) treated as first-class citizens rather than afterthoughts.

### Target Architecture (Full Vision)

- **User Service** — user identity and profile management
- **Wallet Service** — balance management per user
- **Transaction Service** — payment initiation and processing
- **Fraud Detection Service** — real-time transaction risk scoring
- **Notification Service** — async user notifications
- **Ledger Service** — immutable financial record-keeping
- **API Gateway** — single entry point routing to all services

Currently, User/Wallet/Transaction/Fraud Detection all live inside a single `user-service` codebase (by design, for early milestones) — they'll be split into genuinely independent services in Milestone 11.

### Cross-Cutting Concerns

| Concern | Status |
|---|---|
| PostgreSQL (persistence) | ✅ Implemented |
| Kafka (event-driven communication) | ✅ Implemented |
| Redis (idempotency, rate limiting) | ✅ Implemented |
| Rules-based fraud detection | ✅ Implemented |
| Docker (full stack containerized) | 🔄 In progress (Milestone 10) |
| Distributed locking | ⬜ Planned (Milestone 11) |
| JWT Authentication + Spring Security | ⬜ Planned |
| Monitoring / observability | ⬜ Planned (Milestone 12) |
| Dead Letter Queue / retry topics | ⬜ Planned |
| Transactional Outbox Pattern | ⬜ Planned (known limitation — see below) |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Build Tool | Maven (with Maven Wrapper) |
| Persistence | PostgreSQL 16, Spring Data JPA, Flyway |
| Caching / Idempotency / Rate Limiting | Redis 7.4 |
| Messaging | Apache Kafka 3.8 (KRaft mode) |
| Containerization | Docker Compose |
| API Docs | springdoc-openapi (Swagger UI) |
| Security | Spring Security, JWT (planned) |

---

## Architecture Principles

- **Layered / Clean Architecture** — dependencies flow inward; services never expose internal entities directly
- **DTOs at every boundary** — request/response shapes are decoupled from persistence models
- **Constructor injection only** — no field injection, for immutability and testability
- **Rich domain model** — invariant-enforcing logic (e.g. `Wallet.credit()`/`debit()`) lives on entities, not scattered across services
- **Explicit transaction boundaries** — `TransactionTemplate` used over `@Transactional` where self-invocation or precise post-commit ordering matters (e.g. publishing Kafka events only after a DB commit)
- **Defense in depth** — invariants validated at the DTO layer (Bean Validation), domain layer (entity methods), and database layer (constraints), independently
- **Atomic check-then-act** — idempotency keys and rate limits use Redis's atomic primitives (`SETNX`, `INCR`) to avoid the same class of race condition that `@Version` optimistic locking solves at the database layer
- **Each service is independently named and structured** (`com.visasim.<service-name>`) to reinforce true microservice ownership, even while co-located in this practice monorepo
- **Conventional Commits** for all git history

---

## Repository Structure

```
Distributed-Payment-Processing-Platform/
├── docker-compose.yml
└── user-service/
    ├── Dockerfile
    ├── .dockerignore
    ├── pom.xml
    ├── .gitignore
    └── src/
        ├── main/
        │   ├── java/com/visasim/userservice/
        │   │   ├── controller/
        │   │   ├── service/
        │   │   ├── dto/
        │   │   ├── model/
        │   │   ├── repository/
        │   │   ├── event/
        │   │   ├── listener/
        │   │   ├── filter/
        │   │   ├── config/
        │   │   └── exceptions/
        │   └── resources/
        │       ├── application.yml
        │       └── db/migration/        (Flyway SQL migrations, V1–V5+)
        └── test/
```

Additional services (`wallet-service`, `transaction-service`, etc.) will be split out as true siblings of `user-service` in Milestone 11.

---

## Development Roadmap

| # | Milestone | Status |
|---|---|---|
| 1 | Spring Boot Project Setup | ✅ Done |
| 2 | REST API (Users) | ✅ Done |
| 3 | PostgreSQL Integration | ✅ Done |
| 4 | Wallets | ✅ Done |
| 5 | Transactions | ✅ Done |
| 6 | Concurrency (optimistic locking) | ✅ Done |
| 7 | Kafka (event-driven architecture) | ✅ Done |
| 8 | Redis (idempotency + rate limiting) | ✅ Done |
| 9 | Fraud Detection | ✅ Done |
| 10 | Docker (containerize the app itself) | 🔄 In progress |
| 11 | Microservices (multi-service split) | ⬜ Not started |
| 12 | Monitoring | ⬜ Not started |
| 13 | Performance Testing | ⬜ Not started |
| 14 | Deployment | ⬜ Not started |

---

## What's Implemented So Far

### Milestone 1 — Spring Boot Project Setup ✅
- Bootstrapped `user-service` on Spring Boot 3.3.4 / Java 21
- Maven Wrapper for reproducible builds
- Embedded Tomcat on port `8081`, Actuator health check wired in

### Milestone 2 — REST API ✅
- `User` domain model, immutable record-based DTOs, Bean Validation
- `POST /users`, `GET /users/{id}`
- `GlobalExceptionHandler` (`@RestControllerAdvice`) for consistent error responses
- Constructor-based dependency injection throughout

### Milestone 3 — PostgreSQL Integration ✅
- PostgreSQL 16 via Docker, Spring Data JPA, Flyway migrations
- `ddl-auto: validate` — Hibernate never auto-modifies schema; Flyway owns all schema changes
- `User` converted to a real `@Entity`, backed by `UserRepository`

### Milestone 4 — Wallets ✅
- `Wallet` entity, one-to-one with `User`, `BigDecimal`/`NUMERIC(19,4)` for money (never floating point)
- Domain-level `credit()`/`debit()` methods enforcing invariants (balance can never go negative)
- `CreditRequest`/`DebitRequest` DTOs with `@DecimalMin` validation

### Milestone 5 — Transactions ✅
- `Transaction` entity, append-only, starts `PENDING` → `COMPLETED`/`FAILED`
- Atomic wallet-to-wallet `transfer()` — full rollback on failure
- `TransactionAuditService` using `Propagation.REQUIRES_NEW` so failed-transaction audit records survive rollback of the main transfer

### Milestone 6 — Concurrency ✅
- Reproduced a real lost-update race condition under concurrent wallet credits (10-thread test)
- Fixed with `@Version` optimistic locking
- `WalletService` refactored to use `TransactionTemplate` with retry + exponential backoff, avoiding Spring's self-invocation proxy limitation entirely

### Milestone 7 — Kafka ✅
- Kafka (KRaft mode, no Zookeeper) via Docker
- `TransactionCompletedEvent` as an explicit event contract, decoupled from internal DTOs
- Events keyed by `fromWalletId` to preserve per-wallet ordering across partitions
- Producer publishes only *after* the DB transaction commits (mitigates, but doesn't fully solve, the dual-write problem — see Known Limitations)
- Stand-in consumer (`TransactionEventListener`) — future home of the real Notification Service

### Milestone 8 — Redis ✅
- Idempotency keys on `/transactions/transfer`, enforced via atomic `SETNX` with 24h TTL
- Fixed-window rate limiting (20 req/min per IP) via atomic `INCR`, returns `429` when exceeded
- **Known limitation:** a failed transfer still permanently consumes its idempotency key (see below)

### Milestone 9 — Fraud Detection ✅
- Rules-based risk engine: self-transfer check, Redis-backed velocity check, wallet-relative large-amount check
- Weighted risk scoring → `ALLOW` / `FLAG` / `BLOCK` decisions
- `fraud_checks` table as a full audit trail — including blocked attempts that never became a `Transaction` (`transaction_id` is nullable by design)
- `BLOCK` returns `403 Forbidden`; `FLAG` allows the transfer but logs it for review

### Milestone 10 — Docker 🔄
- Multi-stage `Dockerfile` (JDK build stage → slim JRE runtime stage)
- `user-service` added to `docker-compose.yml`, networked with Postgres/Kafka/Redis via Compose service-name hostnames
- Goal: full stack (`app` + all infra) starts with a single `docker compose up -d --build`

---

## Known Limitations (Deliberate, Flagged Honestly)

- **Dual-write problem (Kafka):** the DB commit and the Kafka publish are two separate operations. Publishing after commit avoids "announcing" a transfer that never happened, but a Kafka publish failure *after* a successful commit would still go unnoticed. The correct fix — the **Transactional Outbox Pattern** — is a planned future extension.
- **Idempotency key lifecycle (Redis):** currently, a failed transfer still burns its idempotency key for 24h, incorrectly blocking legitimate retries after a fix. The correct fix is to only mark the key as used on success, or cache-and-replay the response.
- **Fixed-window rate limiting:** simple and effective, but allows short bursts across a window boundary. A sliding-window or token-bucket algorithm would close this gap.

---

## Running the Project

### Option A — Full stack via Docker Compose (recommended)

```bash
docker compose up -d --build
```

Brings up Postgres, Kafka, Redis, and `user-service` together. No local Java/Maven setup required.

### Option B — App locally, infra via Docker

```bash
docker compose up -d postgres kafka redis
cd user-service
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw spring-boot:run
```

### Verify it's alive

```bash
curl http://localhost:8081/actuator/health
```

### Interactive API testing

Swagger UI: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

Or use the included test script, which chains user/wallet/transfer creation and auto-extracts IDs:
```bash
./test-flow.sh
```

---

## Example Requests

**Create a user:**
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{"fullName": "Aadidev", "email": "aadidev@example.com"}'
```

**Create a wallet for a user:**
```bash
curl -X POST http://localhost:8081/wallets/users/{userId}
```

**Credit a wallet:**
```bash
curl -X POST http://localhost:8081/wallets/{walletId}/credit \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00}'
```

**Transfer between wallets (idempotent):**
```bash
curl -X POST http://localhost:8081/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromWalletId": "...", "toWalletId": "...", "amount": 25.00, "idempotencyKey": "'$(uuidgen)'"}'
```

---

## Requirements

- Docker Desktop (recommended path), **or**
- Java 21 (LTS) + Docker Desktop for infra-only (Maven not required — wrapper included)

---

## License

This is a personal learning project and does not currently carry an open-source license.
