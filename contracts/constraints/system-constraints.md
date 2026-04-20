# System constraints

## Functional constraints

- Only json, yaml, and yml files are allowed
- Maximum upload file size is configurable
- Uploaded files are stored on local disk for MVP
- Contract metadata is stored in PostgreSQL
- Analysis must be asynchronous through RabbitMQ
- Failed analysis must support retry and dead-letter handling
- Status flow is:
  - PENDING
  - PROCESSING
  - COMPLETED
  - FAILED

## Breaking change constraints

- Removed path = breaking change
- Removed HTTP method on an existing path = breaking change
- Added path alone = not a breaking change
- Added method alone = not a breaking change

## Technical constraints

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- RabbitMQ
- Angular
- Flyway
- Testcontainers
- ArchUnit
- Docker Compose

## Architecture constraints

- Modular monolith
- No microservices in MVP
- No authentication in MVP
- No Kubernetes in MVP
- No Redis
- No Elasticsearch
- No CQRS
- No event sourcing
- No reactive stack unless absolutely necessary
