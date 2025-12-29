#!/usr/bin/env bash
#
# Maven Integration Test Script (Local)
#
# This script tests the locally-built Maven artifact by:
# 1. Installing the artifact to local Maven repository
# 2. Building a simple Java app that uses EmbeddedValidator
# 3. Running it to verify no ClassNotFoundException occurs
# 4. Checking that shaded classes are present
#
# Usage:
#   ./test-maven-integration.sh [local|VERSION]
#
# - "local" (default): Test the locally-built Maven artifact from build/dist/
# - VERSION: Test a specific version from Maven Central

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

MODE="${1:-local}"

echo "========================================"
echo "Maven Integration Test"
echo "========================================"

if [ "$MODE" = "local" ]; then
  echo "Testing locally-built Maven artifact"
  echo ""
  
  # Find the locally-built Maven artifact
  VALIDATOR_ROOT="$SCRIPT_DIR/../.."
  DIST_DIR="$VALIDATOR_ROOT/build/dist"
  MAVEN_ARTIFACTS_DIR="$DIST_DIR/nu/validator/validator"
  
  if [ ! -d "$MAVEN_ARTIFACTS_DIR" ]; then
    echo "Error: Maven artifacts directory not found at: $MAVEN_ARTIFACTS_DIR"
    echo "Please run: python checker.py maven-artifacts"
    exit 1
  fi
  
  # Find the version directory (there should be only one)
  mapfile -t VERSION_DIRS < <(find "$MAVEN_ARTIFACTS_DIR" -maxdepth 1 -type d -name "*.*.*" 2>/dev/null | sort -V)
  
  if [ ${#VERSION_DIRS[@]} -eq 0 ]; then
    echo "Error: No version directory found in $MAVEN_ARTIFACTS_DIR"
    echo "Please run: python checker.py maven-artifacts"
    exit 1
  fi
  
  # Use the latest version
  VERSION_DIR="${VERSION_DIRS[-1]}"
  VERSION=$(basename "$VERSION_DIR")
  
  echo "Found locally-built version: $VERSION"
  echo "Artifact directory: $VERSION_DIR"
  echo ""
  
  # Check for required files
  JAR_FILE="$VERSION_DIR/validator-$VERSION.jar"
  POM_FILE="$VERSION_DIR/validator-$VERSION.pom"
  
  if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    exit 1
  fi
  
  if [ ! -f "$POM_FILE" ]; then
    echo "Error: POM file not found: $POM_FILE"
    exit 1
  fi
  
  echo "Found JAR: $(basename "$JAR_FILE") ($(du -h "$JAR_FILE" | cut -f1))"
  echo "Found POM: $(basename "$POM_FILE")"
  echo ""
  
  # Install to local Maven repository
  echo "Installing artifact to local Maven repository..."
  if ! mvn install:install-file \
    -Dfile="$JAR_FILE" \
    -DpomFile="$POM_FILE" \
    -DgroupId=nu.validator \
    -DartifactId=validator \
    -Dversion="$VERSION" \
    -Dpackaging=jar; then
    echo "Error: Failed to install artifact to local Maven repository"
    exit 1
  fi
  
  echo "✓ Artifact installed to local Maven repository"
  echo ""
  
else
  # Test version from Maven Central
  VERSION="$MODE"
  echo "Testing validator version from Maven Central: $VERSION"
  echo ""
  
  # If version is "latest", resolve it from Maven Central
  if [ "$VERSION" = "latest" ]; then
    echo "Resolving latest version from Maven Central..."
    MAVEN_METADATA_URL="https://repo1.maven.org/maven2/nu/validator/validator/maven-metadata.xml"
    
    if command -v curl &> /dev/null; then
      MAVEN_METADATA=$(curl -fsSL "$MAVEN_METADATA_URL")
    elif command -v wget &> /dev/null; then
      MAVEN_METADATA=$(wget -qO- "$MAVEN_METADATA_URL")
    else
      echo "Error: Neither curl nor wget found. Cannot fetch Maven metadata."
      exit 1
    fi
    
    # Extract latest version using grep and sed (portable)
    VERSION=$(echo "$MAVEN_METADATA" | grep '<latest>' | sed 's/.*<latest>\(.*\)<\/latest>.*/\1/' | tr -d ' ')
    
    if [ -z "$VERSION" ]; then
      echo "Error: Could not resolve latest version from Maven Central"
      exit 1
    fi
    
    echo "Resolved version: $VERSION"
    echo ""
  fi
fi

# Update pom.xml with the version (portable sed)
echo "Updating pom.xml with version $VERSION..."
if [ "$(uname)" = "Darwin" ]; then
  # macOS
  sed -i '' "s/VALIDATOR_VERSION_PLACEHOLDER/$VERSION/g" pom.xml
else
  # Linux
  sed -i "s/VALIDATOR_VERSION_PLACEHOLDER/$VERSION/g" pom.xml
fi

cleanup() {
  # Restore pom.xml on exit
  if [ -f pom.xml.bak ]; then
    mv pom.xml.bak pom.xml
  fi
  if [ "$(uname)" != "Darwin" ]; then
    # On Linux, sed -i doesn't create backup, so restore from git
    if command -v git &> /dev/null && git rev-parse --git-dir > /dev/null 2>&1; then
      git checkout -- pom.xml 2>/dev/null || true
    fi
  fi
}

trap cleanup EXIT

# Backup pom.xml
cp pom.xml pom.xml.bak

# Ensure Maven is available
if ! command -v mvn &> /dev/null; then
  echo "Error: Maven (mvn) is not installed or not in PATH"
  exit 1
fi

echo "Maven version:"
mvn --version
echo ""

# Clean and compile
echo "Compiling test..."
mvn clean compile

# Run the test
echo ""
echo "Running integration test..."
mvn exec:java

echo ""
echo "========================================"
echo "✅ Maven Integration Test Passed"
echo "========================================"
