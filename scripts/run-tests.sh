#!/bin/bash
# Usage: ./run-tests.sh [profile] [suite]
# Example: ./run-tests.sh default testng.xml

PROFILE=${1:-default}
SUITE=${2:-testng.xml}

echo "Running Maven tests with profile: $PROFILE and suite: $SUITE"
mvn clean test -P $PROFILE -DsuiteXmlFile=$SUITE
