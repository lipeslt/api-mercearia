#!/bin/bash
# Carrega variáveis do .env e sobe o backend
set -a
source "$(dirname "$0")/.env"
set +a
exec mvn spring-boot:run
