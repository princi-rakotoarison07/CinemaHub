#!/usr/bin/env bash
set -euo pipefail

# Script to run the Spring Boot backend for development.
# Usage: ./run-backend.sh

cd "$(dirname "$0")"

echo "Working directory: $(pwd)"

if [ ! -x ./mvnw ]; then
  echo "Warning: './mvnw' not found or not executable in $(pwd)."
  echo "If you have Maven installed system-wide, replace './mvnw' with 'mvn'."
fi

echo "Running backend (Spring Boot)..."
./mvnw spring-boot:run
