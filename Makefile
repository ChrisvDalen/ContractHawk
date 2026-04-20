.PHONY: help up down logs backend frontend test backend-test frontend-test generate clean

help:
	@echo "Contract Hawk - common tasks"
	@echo "  make up             Start Postgres + RabbitMQ + backend + frontend via Docker Compose"
	@echo "  make down           Stop Docker Compose stack"
	@echo "  make logs           Tail Docker Compose logs"
	@echo "  make backend        Run backend locally (Spring Boot)"
	@echo "  make frontend       Run frontend locally (Angular dev server)"
	@echo "  make test           Run backend + frontend tests"
	@echo "  make backend-test   Run backend tests only"
	@echo "  make frontend-test  Run frontend tests only"
	@echo "  make generate       Regenerate OpenAPI client/server stubs"
	@echo "  make clean          Remove build artifacts"

up:
	docker compose up -d --build

down:
	docker compose down

logs:
	docker compose logs -f

backend:
	cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

frontend:
	cd frontend && npm start

test: backend-test frontend-test

backend-test:
	cd backend && ./mvnw test

frontend-test:
	cd frontend && npm test -- --watch=false --browsers=ChromeHeadless

generate:
	cd backend && ./mvnw generate-sources
	cd frontend && npm run generate:api

clean:
	cd backend && ./mvnw clean || true
	rm -rf frontend/dist frontend/node_modules
