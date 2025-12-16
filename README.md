# API Contract Registry

Interne API catalogus voor teams met API's, endpoints en changelog. Zoek/filter/sort functionaliteit.

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.4.0
- Maven
- PostgreSQL
- Flyway
- JPA/Hibernate
- OpenAPI/Swagger UI
- Lombok

### Frontend
- Angular 21
- Standalone components
- Reactive Forms
- HttpClient
- TypeScript

### Infrastructure
- Docker Compose
- PostgreSQL 16
- Nginx (voor frontend)

## Vereisten

- JDK 21
- Node.js 20+
- Docker & Docker Compose (optioneel)

## Lokaal draaien (zonder Docker)

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend draait op: http://localhost:8080

### Frontend

```bash
cd frontend
npm install
npm start
```

Frontend draait op: http://localhost:4200

**Let op:** Zorg dat PostgreSQL draait op localhost:5432 met database `contracthawk`, gebruiker `contracthawk` en wachtwoord `contracthawk`.

## Docker Compose

Start alles met één commando:

```bash
docker compose up --build
```

Dit start:
- PostgreSQL op poort 5432
- Backend op poort 8080
- Frontend op poort 4200

## URLs

- **UI:** http://localhost:4200
- **API:** http://localhost:8080/api/apis
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs

## API Endpoints

### API Contracts

- `GET /api/apis` - Lijst alle API contracts (met search/filter/sort)
- `GET /api/apis/{id}` - Haal API contract details op
- `POST /api/apis` - Maak nieuw API contract
- `PUT /api/apis/{id}` - Update API contract
- `PATCH /api/apis/{id}/lifecycle` - Update lifecycle
- `DELETE /api/apis/{id}` - Verwijder API contract

### Endpoints

- `POST /api/apis/{id}/endpoints` - Voeg endpoint toe
- `PUT /api/apis/{id}/endpoints/{endpointId}` - Update endpoint
- `DELETE /api/apis/{id}/endpoints/{endpointId}` - Verwijder endpoint

### Changelog

- `POST /api/apis/{id}/changelog` - Voeg changelog entry toe
- `DELETE /api/apis/{id}/changelog/{entryId}` - Verwijder changelog entry

## Query Parameters (GET /api/apis)

- `q` - Zoek in name, baseUrl, ownerTeam, version, description
- `lifecycle` - Filter op lifecycle (DRAFT, ACTIVE, DEPRECATED)
- `ownerTeam` - Filter op owner team
- `sort` - Sorteer op: name, updatedAt, ownerTeam, lifecycle
- `dir` - Sorteer richting: asc, desc

## Database Schema

### api_contract
- id (UUID, PK)
- name (VARCHAR(80), uniek per owner_team)
- base_url (VARCHAR(200))
- version (VARCHAR(40))
- owner_team (VARCHAR(60))
- lifecycle (ENUM: DRAFT, ACTIVE, DEPRECATED)
- open_api_url (VARCHAR(300), optioneel)
- description (VARCHAR(400), optioneel)
- created_at, updated_at (TIMESTAMP)

### endpoint
- id (UUID, PK)
- api_id (UUID, FK)
- method (ENUM: GET, POST, PUT, PATCH, DELETE)
- path (VARCHAR(200))
- description (VARCHAR(300), optioneel)
- deprecated (BOOLEAN)
- created_at (TIMESTAMP)
- Uniek constraint: (api_id, method, path)

### changelog_entry
- id (UUID, PK)
- api_id (UUID, FK)
- type (ENUM: ADDED, CHANGED, DEPRECATED, REMOVED, FIXED)
- breaking (BOOLEAN)
- summary (VARCHAR(200))
- details (VARCHAR(1000), optioneel)
- released_at (TIMESTAMP)

## Tests

### Backend

```bash
cd backend
mvn test
```

### Frontend

```bash
cd frontend
npm test
```

## Project Structuur

```
.
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/contracthawk/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   ├── controller/
│   │   │   │   ├── exception/
│   │   │   │   └── mapper/
│   │   │   └── resources/
│   │   │       ├── db/migration/
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── pages/
│   │   │   ├── services/
│   │   │   ├── models/
│   │   │   └── environments/
│   │   └── styles.css
│   ├── package.json
│   └── angular.json
├── docker-compose.yml
└── README.md
```

## Features

- ✅ Volledige CRUD voor API contracts
- ✅ Endpoint beheer per API
- ✅ Changelog beheer per API
- ✅ Zoeken en filteren
- ✅ Sorteren op verschillende velden
- ✅ Validatie (client & server)
- ✅ Error handling met duidelijke foutmeldingen
- ✅ Swagger/OpenAPI documentatie
- ✅ Responsive UI
- ✅ Docker support

## Licentie

Interne tool voor team gebruik.

