#!/usr/bin/env bash
#
# Maven Integration Test Script
#
# Tests the locally-built Maven artifact by:
# 1. Installing the artifact to local Maven repository
# 2. Building a Java app that depends on it
# 3. Running tests to verify shaded classes are correct
#
# Usage: ./test-maven-integration.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================"
echo "Maven Integration Test"
echo "========================================"
echo "Testing locally-built Maven artifact"
echo ""

VALIDATOR_ROOT="$SCRIPT_DIR/../.."
MAVEN_ARTIFACTS_DIR="$VALIDATOR_ROOT/build/dist/nu/validator/validator"

if [ ! -d "$MAVEN_ARTIFACTS_DIR" ]; then
  echo "Error: Maven artifacts not found at: $MAVEN_ARTIFACTS_DIR"
  echo "Run: python checker.py maven-artifacts"
  exit 1
fi

# Find version directories (format: X.Y.Z)
mapfile -t VERSION_DIRS < <(find "$MAVEN_ARTIFACTS_DIR" -maxdepth 1 -type d -name "*.*.*" 2>/dev/null | sort -V)

if [ ${#VERSION_DIRS[@]} -eq 0 ]; then
  echo "Error: No version directory found in $MAVEN_ARTIFACTS_DIR"
  echo "Run: python checker.py maven-artifacts"
  exit 1
fi

VERSION_DIR="${VERSION_DIRS[-1]}"
VERSION=$(basename "$VERSION_DIR")

JAR_FILE="$VERSION_DIR/validator-$VERSION.jar"
POM_FILE="$VERSION_DIR/validator-$VERSION.pom"

if [ ! -f "$JAR_FILE" ]; then
  echo "Error: JAR not found: $JAR_FILE"
  exit 1
fi

if [ ! -f "$POM_FILE" ]; then
  echo "Error: POM not found: $POM_FILE"
  exit 1
fi

echo "Version: $VERSION"
echo "JAR: $(basename "$JAR_FILE") ($(du -h "$JAR_FILE" | cut -f1))"
echo ""

if ! command -v mvn &> /dev/null; then
  echo "Error: Maven (mvn) not found in PATH"
  exit 1
fi

echo "Installing to local Maven repository..."
mvn -q install:install-file \
  -Dfile="$JAR_FILE" \
  -DpomFile="$POM_FILE" \
  -DgroupId=nu.validator \
  -DartifactId=validator \
  -Dversion="$VERSION" \
  -Dpackaging=jar
echo "✓ Installed"
echo ""

echo "Compiling and running test..."
mvn -q clean compile exec:java -Dvalidator.version="$VERSION"

echo ""
echo "========================================"
echo "✅ Maven Integration Test Passed"
echo "========================================"
