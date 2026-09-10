#!/usr/bin/env bash
# Source this file before building. Java 21 is required by Hop and Commons.
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="${JAVA_HOME}/bin:${PATH}"; fi
java_major="$(java -version 2>&1 | head -n 1 | sed -E 's/.*version "([0-9]+).*/\1/')"
if [[ "${java_major}" != 21 ]]; then
  echo "Java 21 required; select a Java 21 JDK using JAVA_HOME (current: ${java_major})." >&2
  exit 1
fi
