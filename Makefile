# BuildTrack — Docker release commands
#
# Docker Compose is the default. With Podman, run:
#   make up COMPOSE="podman compose"
# or use the podman-* targets.

SHELL := /usr/bin/env bash
COMPOSE ?= docker compose
PODMAN_COMPOSE ?= $(shell command -v podman-compose >/dev/null 2>&1 && echo podman-compose || echo podman compose)

# Docker Compose lets the invoking shell override .env. Unset every key defined in
# .env so the file wins, matching what `make prod-check` validates.
UNSET_ENV = $$(grep -E '^[A-Za-z_][A-Za-z0-9_]*=' .env 2>/dev/null | cut -d= -f1 | sed 's/^/-u /' | tr '\n' ' ')

.PHONY: help env build up down restart logs ps health test clean \
        prod-check prod-up prod-down prod-ps prod-logs prod-health backup \
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
	@echo "  Production (Caddy HTTPS profile):"
	@echo "  make prod-check   Validate .env before deploying"
	@echo "  make prod-up      Build and start the stack with automatic HTTPS"
	@echo "  make prod-down    Stop the production stack"
	@echo "  make prod-ps      Show production service status"
	@echo "  make prod-logs    Follow production logs"
	@echo "  make prod-health  Check the public HTTPS health endpoints"
	@echo "  make backup       Back up MySQL and uploaded photos to ./backups"
	@echo ""
	@echo "  Podman: make up COMPOSE=\"podman compose\""
	@echo "          make podman-up / make podman-down"

env:
	./scripts/generate-env.sh

build:
	@env $(UNSET_ENV) $(COMPOSE) build

up:
	@if [ ! -f .env ]; then ./scripts/generate-env.sh; fi
	@env $(UNSET_ENV) $(COMPOSE) up -d --build

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

prod-check:
	./scripts/prod-preflight.sh

prod-up:
	@if [ ! -f .env ]; then ./scripts/generate-env.sh; fi
	@./scripts/prod-preflight.sh
	@env $(UNSET_ENV) $(COMPOSE) --profile proxy up -d --build

prod-down:
	$(COMPOSE) --profile proxy down

prod-ps:
	$(COMPOSE) --profile proxy ps

prod-logs:
	$(COMPOSE) --profile proxy logs -f --tail=200

prod-health:
	@DOMAIN=$$(grep -m1 '^DOMAIN=' .env | cut -d= -f2-); \
	if [ -z "$$DOMAIN" ]; then echo "Set DOMAIN in .env" >&2; exit 1; fi; \
	echo "Backend:"; curl -fsS "https://$$DOMAIN/api/health"; echo; \
	echo "AI service:"; curl -fsS "https://$$DOMAIN/ai/api/health"; echo

backup:
	./deploy/backup-mysql.sh

test:
	cd backend && JAVA_HOME=$${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk} ./mvnw verify
	cd frontend && npm test && npm run build
	cd ai_platform && python3 test_suite.py

clean:
	rm -rf frontend/dist backend/target

podman-build:
	@env $(UNSET_ENV) $(PODMAN_COMPOSE) build

podman-up:
	@if [ ! -f .env ]; then ./scripts/generate-env.sh; fi
	@env $(UNSET_ENV) $(PODMAN_COMPOSE) up -d --build

podman-down:
	$(PODMAN_COMPOSE) down

podman-logs:
	$(PODMAN_COMPOSE) logs -f --tail=200
