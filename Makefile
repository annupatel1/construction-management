# BuildTrack — Docker release commands
#
# Docker Compose is the default. With Podman, run:
#   make up COMPOSE="podman compose"
# or use the podman-* targets.

SHELL := /usr/bin/env bash
COMPOSE ?= docker compose
PODMAN_COMPOSE ?= $(shell command -v podman-compose >/dev/null 2>&1 && echo podman-compose || echo podman compose)

.PHONY: help env build up down restart logs ps health test clean \
        podman-build podman-up podman-down podman-logs

help:
	@echo "BuildTrack Docker commands"
	@echo ""
	@echo "  make env          Create .env with strong generated secrets"
	@echo "  make build        Build all images"
	@echo "  make up           Start the full stack (build if needed)"
	@echo "  make down         Stop and remove containers (keeps the database volume)"
	@echo "  make restart      Restart all services"
	@echo "  make logs         Follow logs"
	@echo "  make ps           Show service status"
	@echo "  make health       Call the backend and AI health endpoints"
	@echo "  make test         Run backend, frontend and AI tests locally"
	@echo "  make clean        Remove local build output (not the database volume)"
	@echo ""
	@echo "  Podman: make up COMPOSE=\"podman compose\""
	@echo "          make podman-up / make podman-down"

env:
	./scripts/generate-env.sh

build:
	$(COMPOSE) build

up:
	@if [ ! -f .env ]; then ./scripts/generate-env.sh; fi
	$(COMPOSE) up -d --build

down:
	$(COMPOSE) down

restart:
	$(COMPOSE) restart

logs:
	$(COMPOSE) logs -f --tail=200

ps:
	$(COMPOSE) ps

health:
	@echo "Backend:"
	@curl -fsS http://localhost:8080/api/health || echo "backend is not healthy"
	@echo
	@echo "AI service:"
	@curl -fsS http://localhost:8000/api/health || echo "AI service is not healthy"
	@echo

test:
	cd backend && JAVA_HOME=$${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk} ./mvnw verify
	cd frontend && npm run build
	cd ai_platform && python3 test_suite.py

clean:
	rm -rf frontend/dist backend/target

podman-build:
	$(PODMAN_COMPOSE) build

podman-up:
	@if [ ! -f .env ]; then ./scripts/generate-env.sh; fi
	$(PODMAN_COMPOSE) up -d --build

podman-down:
	$(PODMAN_COMPOSE) down

podman-logs:
	$(PODMAN_COMPOSE) logs -f --tail=200
