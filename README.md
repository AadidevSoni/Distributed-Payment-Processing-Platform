# Distributed Payment Processing Platform (Visa-Sim)

A miniature, production-quality simulation of a distributed payment processing backend — built as a hands-on learning project to master Spring Boot, Java 21, and the architectural patterns used in real fintech systems (Visa, Amazon, Google, Goldman Sachs-style engineering).

The goal is not maximum features — the goal is **maximum engineering quality**. Every design decision is deliberate and explainable, not copy-pasted.

---

## Project Goal

Build a simplified, distributed version of a Visa-like payment backend, composed of independent microservices communicating over REST and Kafka, backed by PostgreSQL and Redis, with production concerns (idempotency, retries, rate limiting, fraud detection, monitoring, load testing) treated as first-class citizens rather than afterthoughts.

### Architecture

As of Milestone 11, the platform is genuinely split into three independently deployable services, each with its own Maven project, own package root, own database (where applicable), and own Dockerfile:

- **user-service** (`:8081`) — Users, Wallets, Transactions. The core money-moving service.
- **fraud-service** (`:8082`) — Rules-based fraud risk scoring, its own PostgreSQL database.
- **notification-service** — Pure Kafka consumer, no database, no business API. Logs completed transactions; the eventual home of real user notifications.

A Ledger Service and API Gateway remain in the original vision but are not yet built.

### Cross-Cutting Concerns

| Concern | Status |
|---|---|
| PostgreSQL (persistence, database-per-service) | ✅ Implemented |
| Kafka (event-driven communication) | ✅ Implemented |
| Redis (idempotency, rate limiting, fraud velocity) | ✅ Implemented |
| Rules-based fraud detection | ✅ Implemented |
| Docker (full stack containerized) | ✅ Implemented |
| Microservices split | ✅ Implemented |
| Dead Letter Queue (Kafka) | ✅ Implemented |
| Monitoring / observability (Prometheus + Grafana) | 🔄 In progress (Milestone 12) |
| Performance / load testing (k6) | 🔄 In progress (Milestone 13) |
| Distributed tracing | ⬜ Planned |
| JWT Authentication + Spring Security | ⬜ Planned |
| Transactional Outbox Pattern | ⬜ Planned (known limitation — see below) |
| API Gateway | ⬜ Planned |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Build Tool | Maven (with Maven Wrapper, per service) |
| Persistence | PostgreSQL 16 (separate DB per service), Spring Data JPA, Flyway |
| Caching / Idempotency / Rate Limiting | Redis 7.4 |
| Messaging | Apache Kafka 3.8 (KRaft mode), 3 partitions, dead-letter topic on repeated consumer failure |
| Containerization | Docker Compose, multi-stage Dockerfiles per service |
| Monitoring | Prometheus + Grafana (in progress) |
| Load Testing | k6 (in progress) |
| API Docs | springdoc-openapi (Swagger UI) — user-service and fraud-service |
| Security | Spring Security, JWT (planned) |

---

## Architecture Principles

- **Layered / Clean Architecture** — dependencies flow inward; services never expose internal entities directly
- **DTOs at every boundary** — request/response shapes are decoupled from persistence models, including at service-to-service HTTP boundaries
- **Constructor injection only** — no field injection, for immutability and testability
- **Rich domain model** — invariant-enforcing logic (e.g. `Wallet.credit()`/`debit()`) lives on entities, not scattered across services
- **Explicit transaction boundaries** — `TransactionTemplate` used over `@Transactional` where self-invocation or precise post-commit ordering matters (e.g. publishing Kafka events only after a DB commit)
- **Defense in depth** — invariants validated at the DTO layer (Bean Validation), domain layer (entity methods), and database layer (constraints), independently
- **Atomic check-then-act** — idempotency keys, rate limits, and fraud velocity counters all use Redis's atomic primitives (`SETNX`, `INCR`) to avoid the same class of race condition that `@Version` optimistic locking solves at the database layer
- **Database-per-service** — `user-service` and `fraud-service` each own a separate PostgreSQL database with no shared schema and no cross-database foreign keys; referential integrity across that boundary is a known, accepted trade-off (see Known Limitations)
- **Sync where blocking is required, async where it isn't** — `user-service → fraud-service` is a synchronous REST call because a transfer cannot proceed without a fraud decision; `user-service → notification-service` is asynchronous via Kafka because notification delivery has no bearing on whether the payment itself succeeds
- **Each service is independently named, structured, and deployable** (`com.visasim.<service-name>`), with its own `pom.xml`, `Dockerfile`, and (where applicable) database
- **Conventional Commits** for all git history

---

## Repository Structure

```
Distributed-Payment-Processing-Platform/
├── docker-compose.yml
├── prometheus/
│   └── prometheus.yml
├── load-tests/
│   └── transfer-load-test.js
├── test-flow.sh
│
├── user-service/                  :8081
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/visasim/userservice/
│       │   ├── controller/          (Users, Wallets, Transactions)
│       │   ├── service/
│       │   ├── dto/
│       │   ├── model/
│       │   ├── repository/
│       │   ├── client/              (FraudServiceClient)
│       │   ├── event/               (TransactionCompletedEvent)
│       │   ├── filter/               (rate limiting)
│       │   ├── config/
│       │   └── exceptions/
│       └── resources/
│           ├── application.yml
│           └── db/migration/        (V1–V4)
│
├── fraud-service/                 :8082
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/visasim/fraudservice/
│       │   ├── controller/          (FraudCheckController)
│       │   ├── service/             (FraudCheckService)
│       │   ├── client/              (UserServiceClient)
│       │   ├── dto/
│       │   ├── model/
│       │   ├── repository/
│       │   └── config/
│       └── resources/
│           ├── application.yml
│           └── db/migration/        (own V1, own database)
│
└── notification-service/           (Kafka consumer only)
    ├── Dockerfile
    ├── pom.xml
    └── src/main/
        ├── java/com/visasim/notificationservice/
        │   ├── NotificationKafkaConsumer.java
        │   ├── event/               (own copy of TransactionCompletedEvent)
        │   └── config/              (dead-letter error handling)
        └── resources/application.yml
```

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
| 10 | Docker (containerize the app itself) | ✅ Done |
| 11 | Microservices (multi-service split) | ✅ Done |
| 12 | Monitoring (Prometheus + Grafana) | 🔄 In progress |
| 13 | Performance Testing (k6) | 🔄 In progress |
| 14 | Deployment | ⬜ Not started |

---

## What's Implemented So Far

### Milestones 1–9 — Foundation through Fraud Detection ✅
Spring Boot + Java 21 scaffold, REST API with DTOs and global exception handling, PostgreSQL + Flyway, `Wallet` domain model with invariant-enforcing `credit()`/`debit()`, atomic multi-wallet `transfer()` with `TransactionAuditService` (`Propagation.REQUIRES_NEW`), a reproduced-and-fixed lost-update race condition (`@Version` optimistic locking + `TransactionTemplate` retry), Kafka event publishing (per-wallet key ordering, post-commit publish), Redis-backed idempotency keys and rate limiting, and a rules-based fraud engine (self-transfer, velocity, wallet-relative large-amount checks) producing weighted `ALLOW`/`FLAG`/`BLOCK` decisions with a full audit trail.

### Milestone 10 — Docker ✅
Multi-stage Dockerfile per service (JDK build stage → slim JRE runtime stage). Full stack — every service and every infrastructure dependency — starts with a single `docker compose up -d --build`.

### Milestone 11 — Microservices ✅
- Extracted **fraud-service**: standalone Spring Boot project, own PostgreSQL database (`visasim_fraud`, its own Flyway history), own `pom.xml` with only the dependencies it needs
- Extracted **notification-service**: standalone Kafka consumer, no database, no business API
- `user-service` now calls `fraud-service` synchronously over HTTP (`FraudServiceClient` → `POST /fraud-checks/evaluate`); the in-process method call became a real network call with real new failure modes
- **`PATCH /fraud-checks/{id}/link`** restores the `transaction_id` audit correlation on `fraud_checks` that was lost the moment fraud evaluation moved out-of-process — successful transfers link back to their fraud check; blocked transfers correctly stay unlinked
- Kafka's dead-letter handling added: `DeadLetterPublishingRecoverer` + bounded `FixedBackOff`, so a poison message retries once then routes to a dead-letter topic instead of retrying forever
- `notification-service` consumes with `concurrency=3`, matching the topic's 3 partitions, under its own consumer group

### Milestone 12 — Monitoring 🔄
Micrometer + Prometheus registry being wired into all three services; Grafana dashboards for request rate and error rate. Not yet fully verified end-to-end — see Known Limitations.

### Milestone 13 — Performance Testing 🔄
k6 load test against `POST /transactions/transfer`, ramping virtual users, with p95/p99 latency and error-rate thresholds. In progress.

---

## Known Limitations (Deliberate, Flagged Honestly)

- **Circular service dependency:** `user-service` calls `fraud-service` for a risk decision; `fraud-service` calls back into `user-service`'s `GET /transactions/history/{walletId}` for its large-amount rule. Neither service can be fully tested or deployed in true isolation as a result. The correct fix — having `fraud-service` build its own local read-model from the `transaction-events` Kafka topic instead of a synchronous callback — is a planned extension, not yet implemented.
- **Dual-write problem (Kafka):** the DB commit and the Kafka publish are two separate operations. Publishing after commit avoids "announcing" a transfer that never happened, but a Kafka publish failure *after* a successful commit would still go unnoticed. The correct fix — the **Transactional Outbox Pattern** — remains a planned extension.
- **Idempotency key lifecycle (Redis):** a failed transfer still burns its idempotency key for 24h, incorrectly blocking legitimate retries after a fix. The correct fix is to only mark the key as used on success, or cache-and-replay the response.
- **Fixed-window rate limiting:** simple and effective, but allows short bursts across a window boundary.
- **Lost referential integrity across service boundaries:** `fraud_checks.from_wallet_id` and `.transaction_id` are plain UUIDs with no database-enforced foreign key, since the referenced tables live in a different service's database. Integrity here is an application-level assumption, not a guarantee.
- **No resilience on the `user-service → fraud-service` HTTP call:** no timeout, retry, or circuit breaker configured yet. If `fraud-service` is slow or unreachable, every transfer currently blocks or fails ungracefully.
- **`depends_on` in Docker Compose guarantees container start order only**, not readiness — relevant now that `user-service`, `fraud-service`, and their databases all depend on each other starting in a reasonable sequence.

---

## Running the Project

### Full stack via Docker Compose

```bash
docker compose up -d --build
```

Brings up Postgres (×2, one per service that needs it), Kafka, Redis, and all three application services together.

### Verify services are alive

```bash
curl http://localhost:8081/actuator/health   # user-service
curl http://localhost:8082/actuator/health   # fraud-service
```

### Interactive API testing

Swagger UI:
- [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) — user-service
- [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) — fraud-service

Or the included regression script, which chains user/wallet/transfer creation and auto-extracts IDs:
```bash
./test-flow.sh
```

### Inspecting each service's data directly

```bash
docker exec -it visasim-postgres psql -U visasim -d visasim              # user-service DB
docker exec -it visasim-fraud-postgres psql -U visasim -d visasim_fraud  # fraud-service DB
docker exec -it visasim-redis redis-cli                                  # idempotency + velocity keys
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

**Transfer between wallets (idempotent, fraud-checked, published to Kafka on success):**
```bash
curl -X POST http://localhost:8081/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromWalletId": "...", "toWalletId": "...", "amount": 25.00, "idempotencyKey": "'$(uuidgen)'"}'
```

---

## Requirements

- Docker Desktop (required — the full stack is now multi-service and expects to run via Compose)
- Java 21 (LTS), if running any service outside Docker for local development
- [k6](https://k6.io) for load testing (`brew install k6`)

---

## License

This is a personal learning project and does not currently carry an open-source license.
