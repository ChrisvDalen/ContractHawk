# 🦅 ContractHawk

> **Developer-first API Contract Registry** — Your single source of truth for API documentation, endpoints, and changelogs.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-21-red)](https://angular.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Internal-yellow)](LICENSE)

---

## 📋 Table of Contents

- [About](#about)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Development](#development)
- [Project Structure](#project-structure)

---

## 🎯 About

**ContractHawk** is an internal developer tool designed to centralize API contract management across teams. As microservices and distributed systems grow, keeping track of API ownership, versions, endpoints, and breaking changes becomes increasingly challenging.

**The Problem:**
- 🔀 Swagger docs scattered across multiple repos
- 👥 Unclear API ownership and team responsibilities  
- 💥 Breaking changes go unnoticed until production
- 🔄 Version drift causes integration headaches

**The Solution:**
ContractHawk provides a **unified catalog** where teams can:
- Register and document APIs with clear ownership
- Manage endpoints with method, path, and deprecation status
- Track changelogs with breaking change indicators
- Search and filter APIs by name, team, lifecycle, or version

**What ContractHawk is NOT:**
- ❌ Not an API gateway
- ❌ Not a service mesh
- ❌ Not a runtime dependency

It's purely a **visibility and documentation tool** for better contract discipline.

---

## ✨ Features

### Core Capabilities

- **📚 API Catalog Management**
  - Full CRUD operations for API contracts
  - Lifecycle management (DRAFT → ACTIVE → DEPRECATED)
  - Unique constraints per team (name + ownerTeam)

- **🔌 Endpoint Tracking**
  - Register HTTP methods and paths per API
  - Mark endpoints as deprecated
  - Prevent duplicate endpoints (method + path)

- **📝 Changelog Management**
  - Track changes with types: ADDED, CHANGED, DEPRECATED, REMOVED, FIXED
  - Mark breaking changes
  - Sort by release date (newest first)

- **🔍 Search & Filter**
  - Full-text search across name, baseUrl, ownerTeam, version, description
  - Filter by lifecycle status
  - Filter by owner team
  - Sort by name, updatedAt, ownerTeam, or lifecycle

- **🎨 Modern UI**
  - Responsive Angular 21 interface
  - Real-time search with debouncing
  - Inline editing for endpoints and changelogs
  - Lifecycle quick actions

- **📖 API Documentation**
  - OpenAPI/Swagger UI integration
  - Interactive API explorer
  - Auto-generated documentation

- **🔄 OpenAPI Sync (V2)**
  - Automatic import from OpenAPI v3 specs (JSON/YAML)
  - MERGE mode: Add/update endpoints without deletions
  - REPLACE mode: Exact sync with spec (removes missing endpoints)
  - Preview diff before importing
  - Breaking change detection (removed/changed endpoints)
  - Scheduled automatic refresh (configurable cron)
  - Sync history tracking with breaking change details
  - Auto-generated changelog entries on sync

---

## 🛠 Tech Stack

### Backend
- **Java 21** - Latest LTS with modern language features
- **Spring Boot 3.4** - Enterprise-grade framework
- **Maven** - Dependency management
- **PostgreSQL 16** - Robust relational database
- **Flyway** - Database migrations
- **JPA/Hibernate** - Object-relational mapping
- **SpringDoc OpenAPI** - API documentation
- **Swagger Parser** - OpenAPI v3 parsing (JSON/YAML)
- **WebFlux** - Reactive HTTP client for fetching specs
- **Spring Scheduler** - Scheduled tasks for auto-refresh
- **Lombok** - Reduced boilerplate

### Frontend
- **Angular 21** - Modern reactive framework
- **Standalone Components** - Latest Angular architecture
- **Reactive Forms** - Form validation and handling
- **TypeScript 5.9** - Type-safe development
- **RxJS** - Reactive programming

### Infrastructure
- **Docker Compose** - Container orchestration
- **Nginx** - Frontend reverse proxy
- **Multi-stage builds** - Optimized Docker images

---

## 🚀 Quick Start

### Prerequisites

- **JDK 21** ([Download](https://adoptium.net/))
- **Node.js 20+** ([Download](https://nodejs.org/))
- **Docker & Docker Compose** (optional, for containerized setup)
- **PostgreSQL 16** (if running locally without Docker)

### Option 1: Docker Compose (Recommended)

Start everything with a single command:

```bash
docker compose up --build
```

This will start:
- 🐘 PostgreSQL on port `5432`
- ☕ Backend API on port `8080`
- 🎨 Frontend UI on port `4200`

**Access points:**
- **UI:** http://localhost:4200
- **API:** http://localhost:8080/api/apis
- **Swagger:** http://localhost:8080/swagger-ui.html

### Option 2: Local Development

#### Backend Setup

```bash
cd backend

# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run
```

**Note:** Ensure PostgreSQL is running locally with:
- Database: `contracthawk`
- Username: `contracthawk`
- Password: `contracthawk`
- Port: `5432`

#### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

Frontend will be available at http://localhost:4200

---

## 📚 API Documentation

### Base URL

```
http://localhost:8080/api/apis
```

### Key Endpoints

#### API Contracts

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/apis` | List all APIs (with search/filter/sort) |
| `GET` | `/api/apis/{id}` | Get API details with endpoints and changelog |
| `POST` | `/api/apis` | Create new API contract |
| `PUT` | `/api/apis/{id}` | Update API contract |
| `PATCH` | `/api/apis/{id}/lifecycle` | Update lifecycle status |
| `DELETE` | `/api/apis/{id}` | Delete API contract (cascades) |

#### Endpoints Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/apis/{id}/endpoints` | Add endpoint to API |
| `PUT` | `/api/apis/{id}/endpoints/{endpointId}` | Update endpoint |
| `DELETE` | `/api/apis/{id}/endpoints/{endpointId}` | Delete endpoint |

#### Changelog Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/apis/{id}/changelog` | Add changelog entry |
| `DELETE` | `/api/apis/{id}/changelog/{entryId}` | Delete changelog entry |

#### OpenAPI Sync (V2)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/apis/{id}/openapi-diff` | Preview diff between current endpoints and OpenAPI spec |
| `POST` | `/api/apis/{id}/import-openapi` | Import endpoints from OpenAPI spec (body: `{ "mode": "MERGE" \| "REPLACE" }`) |
| `GET` | `/api/apis/{id}/sync-runs` | Get sync run history for API |
| `POST` | `/api/admin/refresh-openapi` | Refresh all APIs with openApiUrl (query param: `mode=MERGE\|REPLACE`) |

### Query Parameters

**GET /api/apis** supports:

- `q` - Search query (searches name, baseUrl, ownerTeam, version, description)
- `lifecycle` - Filter by lifecycle (`DRAFT`, `ACTIVE`, `DEPRECATED`)
- `ownerTeam` - Filter by owner team
- `sort` - Sort field (`name`, `updatedAt`, `ownerTeam`, `lifecycle`)
- `dir` - Sort direction (`asc`, `desc`)

### Example Requests

```bash
# Search for APIs
curl "http://localhost:8080/api/apis?q=payment&lifecycle=ACTIVE&sort=name&dir=asc"

# Create new API
curl -X POST http://localhost:8080/api/apis \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Payment API",
    "baseUrl": "https://api.example.com/payments",
    "version": "v1.2.0",
    "ownerTeam": "Platform Team",
    "lifecycle": "ACTIVE",
    "openApiUrl": "https://api.example.com/openapi.json",
    "description": "Handles payment processing"
  }'

# Preview OpenAPI diff
curl "http://localhost:8080/api/apis/{id}/openapi-diff"

# Import OpenAPI (MERGE mode)
curl -X POST http://localhost:8080/api/apis/{id}/import-openapi \
  -H "Content-Type: application/json" \
  -d '{"mode": "MERGE"}'

# Refresh all APIs
curl -X POST "http://localhost:8080/api/admin/refresh-openapi?mode=MERGE"
```

### Interactive API Docs

Visit **http://localhost:8080/swagger-ui.html** for full interactive API documentation with request/response schemas and the ability to test endpoints directly.

---

## 🗄 Database Schema

### `api_contract`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | Primary Key |
| `name` | VARCHAR(80) | NOT NULL, Unique per ownerTeam |
| `base_url` | VARCHAR(200) | NOT NULL |
| `version` | VARCHAR(40) | NOT NULL |
| `owner_team` | VARCHAR(60) | NOT NULL |
| `lifecycle` | ENUM | DRAFT, ACTIVE, DEPRECATED |
| `open_api_url` | VARCHAR(300) | Optional |
| `description` | VARCHAR(400) | Optional |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | NOT NULL |

**Indexes:** `name`, `owner_team`, `lifecycle`, `updated_at`

### `endpoint`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | Primary Key |
| `api_id` | UUID | Foreign Key → api_contract |
| `method` | ENUM | GET, POST, PUT, PATCH, DELETE |
| `path` | VARCHAR(200) | NOT NULL |
| `description` | VARCHAR(300) | Optional |
| `deprecated` | BOOLEAN | Default: false |
| `created_at` | TIMESTAMP | NOT NULL |

**Unique Constraint:** `(api_id, method, path)`

### `changelog_entry`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | Primary Key |
| `api_id` | UUID | Foreign Key → api_contract |
| `type` | ENUM | ADDED, CHANGED, DEPRECATED, REMOVED, FIXED |
| `breaking` | BOOLEAN | Default: false |
| `summary` | VARCHAR(200) | NOT NULL |
| `details` | VARCHAR(1000) | Optional |
| `released_at` | TIMESTAMP | NOT NULL |

**Index:** `(api_id, released_at DESC)`

### `api_sync_run` (V2)

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | Primary Key |
| `api_id` | UUID | Foreign Key → api_contract |
| `run_at` | TIMESTAMP | NOT NULL |
| `status` | ENUM | SUCCESS, FAILED |
| `mode` | ENUM | MERGE, REPLACE |
| `added_count` | INTEGER | Default: 0 |
| `updated_count` | INTEGER | Default: 0 |
| `deleted_count` | INTEGER | Default: 0 |
| `breaks_detected` | BOOLEAN | Default: false |
| `error_message` | VARCHAR(500) | Optional |

**Indexes:** `api_id`, `(api_id, run_at DESC)`

### `api_breaking_change` (V2)

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | Primary Key |
| `sync_run_id` | UUID | Foreign Key → api_sync_run |
| `type` | ENUM | REMOVED_ENDPOINT, PATH_CHANGED, METHOD_CHANGED |
| `method` | VARCHAR(10) | NOT NULL |
| `path` | VARCHAR(200) | NOT NULL |
| `details` | VARCHAR(300) | Optional |

**Index:** `sync_run_id`

---

## 🔄 OpenAPI Sync Configuration

### Scheduled Refresh

ContractHawk supports automatic scheduled refresh of OpenAPI specs. Configure in `application.properties`:

```properties
# Enable scheduled refresh
contracthawk.open-api-refresh.enabled=true

# Cron expression (default: 3 AM daily)
contracthawk.open-api-refresh.cron=0 0 3 * * *

# Default sync mode (MERGE or REPLACE)
contracthawk.open-api-refresh.mode=MERGE
```

**Cron Examples:**
- `0 0 3 * * *` - Daily at 3 AM
- `0 0 */6 * * *` - Every 6 hours
- `0 0 0 * * MON` - Every Monday at midnight
- `0 30 2 * * *` - Daily at 2:30 AM

### Sync Modes

**MERGE Mode:**
- Adds new endpoints from OpenAPI spec
- Updates existing endpoints (description, deprecated status)
- **Does NOT delete** endpoints that are missing from spec
- Safe for production use

**REPLACE Mode:**
- Exact sync with OpenAPI spec
- **Deletes** endpoints not present in spec
- Triggers breaking change detection
- Use with caution - requires confirmation in UI

### Breaking Change Detection

Breaking changes are detected when:
- Endpoint is **removed** (method + path no longer in spec)
- Endpoint **method changes** (same path, different HTTP method)
- Endpoint **path changes** (same method, different path)

Non-breaking changes:
- Description updates
- Deprecated status toggles

---

## 💻 Development

### Running Tests

#### Backend Tests

```bash
cd backend
mvn test
```

Tests include:
- Controller integration tests
- Validation error handling
- Resource not found scenarios
- Duplicate constraint violations

#### Frontend Tests

```bash
cd frontend
npm test
```

Tests include:
- Service HTTP client mocking
- Component form validation
- Error handling

### Code Structure

```
.
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/contracthawk/
│   │   │   │   ├── entity/          # JPA entities
│   │   │   │   ├── dto/             # Data transfer objects
│   │   │   │   ├── repository/      # Data access layer
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── controller/      # REST endpoints
│   │   │   │   ├── exception/       # Error handling
│   │   │   │   ├── mapper/          # Entity-DTO mapping
│   │   │   │   └── specification/  # JPA Specifications
│   │   │   └── resources/
│   │   │       ├── db/migration/    # Flyway migrations
│   │   │       └── application.properties
│   │   └── test/                    # Test suite
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── pages/              # Route components
│   │   │   ├── services/           # HTTP services
│   │   │   ├── models/             # TypeScript interfaces
│   │   │   └── environments/       # Environment config
│   │   └── styles.css
│   ├── package.json
│   └── angular.json
│
├── docker-compose.yml
└── README.md
```

### Error Handling

The API returns structured error responses:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "fieldErrors": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ]
}
```

**Error Codes:**
- `VALIDATION_ERROR` (400) - Invalid input data
- `NOT_FOUND` (404) - Resource doesn't exist
- `DUPLICATE_RESOURCE` (409) - Constraint violation
- `INTERNAL_ERROR` (500) - Server error (includes traceId)

---

## 📁 Project Structure

```
ContractHawk/
├── backend/                 # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                # Angular application
│   ├── src/
│   │   ├── app/
│   │   └── styles.css
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── docker-compose.yml       # Container orchestration
└── README.md
```

---

## 🎨 UI Overview

### API List Page
- Search bar with real-time filtering
- Filter by lifecycle and owner team
- Sortable columns
- Click row to view details

### API Create Page
- Form validation (client + server)
- Clear error messages
- Auto-redirect on success

### API Detail Page
- **Overview Tab:** API metadata and edit mode
- **Endpoints Tab:** List, add, edit, delete endpoints
- **Changelog Tab:** Timeline of changes with breaking badges
- Quick lifecycle actions (DRAFT/ACTIVE/DEPRECATED)
- Delete with confirmation

---

## 📝 License

Internal tool for team use.

---

## 🤝 Contributing

This is an internal project. For issues or feature requests, please contact the platform team.

---

**Built with ❤️ for developers, by developers.**
