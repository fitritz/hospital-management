#!/usr/bin/env bash
set -euo pipefail

# Build hospital-api module and copy its jar to repository root's target/ so
# Railpack can find target/*.jar at /app/target

echo "Building hospital-api..."
mvn -f hospital-api/pom.xml -DskipTests package

echo "Preparing root target directory..."
mkdir -p target
cp hospital-api/target/*.jar target/

echo "Build and copy complete. Jar placed in target/."
