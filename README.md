# Contract Hawk

Contract Hawk is a spec-driven application for uploading, analyzing, and tracking OpenAPI contracts. It detects breaking changes between contract versions and exposes the results through a REST API and an Angular UI.

## Spec-driven workflow

The `contracts/` directory is the source of truth for the behavior of this system. Any implementation in `backend/` or `frontend/` must conform to it.

```
contracts/
  openapi/
    contract-hawk-api.yaml        # HTTP contract (generates server + client stubs)
  specs/
    upload-contract.md            # Feature: upload contract
    analyze-contract.md           # Feature: analyze contract asynchronously
    breaking-change-detection.md  # Feature: detect breaking changes
  constraints/
    system-constraints.md         # Functional, breaking-change, tech, architecture constraints
  verification/
    acceptance-checks.md          # Acceptance criteria (API, analysis, arch, observability)
```

When changing behavior, update the relevant spec or constraint first, then the code, then verification.

## Project layout

```
contract-hawk/
  contracts/     # Spec, constraints, verification (source of truth)
  backend/       # Spring Boot 3 modular monolith (Java 21, Maven)
  frontend/      # Angular app (standalone components)
  docker/        # Dockerfiles
  compose.yaml   # Local Postgres + RabbitMQ + backend + frontend
  Makefile       # Common tasks
```

### Backend packages (modular monolith)

```
com.chrisvdalen.contracthawk
  contract/      # upload + metadata
  analysis/      # async analysis
  messaging/     # RabbitMQ wiring (exchange, queue, DLQ, retry)
  storage/       # file persistence on local disk
  shared/        # exception handling, api glue, config, utilities
  generated/openapi/  # OpenAPI-generated interfaces (build output)
```

ArchUnit tests enforce:

- Controllers do not access repositories directly
- Domain does not depend on controllers
- No cyclic dependencies between feature packages

### Frontend structure

```
src/app/
  core/        # layout, interceptors, cross-cutting services
  shared/      # reusable components, pipes, utilities
  features/
    contracts/
      pages/       # contracts-overview, contract-detail, contract-upload
      components/  # contract-table, contract-upload-form, analysis-summary-card
      services/    # feature services
      models/      # feature view models
  api/generated/ # OpenAPI-generated TypeScript client
```

## Tech stack

- Java 21, Spring Boot 3, Maven
- PostgreSQL, Flyway migrations
- RabbitMQ (retry + dead-letter)
- Angular (standalone components)
- Testcontainers, ArchUnit
- Docker Compose

## Getting started

```bash
# Start Postgres, RabbitMQ, backend, and frontend
make up

# Or run them locally
make backend    # http://localhost:8080
make frontend   # http://localhost:4200
```

## Regenerate OpenAPI stubs

Both the Spring controller interfaces and the Angular TypeScript client are generated from `contracts/openapi/contract-hawk-api.yaml`:

```bash
make generate
```

## Tests

```bash
make test            # backend + frontend
make backend-test
make frontend-test
```

## Acceptance

`contracts/verification/acceptance-checks.md` lists the checks that must pass for the system to be considered conformant to the specs.
