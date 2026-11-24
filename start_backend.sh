#!/bin/sh
cd "$(dirname "$0")"
if command -v mvn >/dev/null 2>&1; then
  mvn -q compile exec:java
else
  echo "Maven was not found. Starting demo mode without MySQL persistence."
  javac *.java && java Main
fi
