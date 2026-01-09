#!/usr/bin/env bash
set -euo pipefail

# Script to install dependencies and run the frontend dev server (Vite).
# Usage: ./run-frontend.sh

cd "$(dirname "$0")"

echo "Working directory: $(pwd)"

echo "Installing npm dependencies (if needed)..."
npm install

echo "Starting frontend dev server (vite)..."
npm run dev
