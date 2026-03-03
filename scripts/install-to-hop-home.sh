#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
VERSION="$(sed -n 's|.*<version>\(.*\)</version>.*|\1|p' "${ROOT_DIR}/pom.xml" | head -n 1)"
HOP_HOME="${1:-${HOP_HOME:-}}"

if [[ -z "${HOP_HOME}" ]]; then
  echo "Usage: $0 /path/to/hop-home"
  echo "or set HOP_HOME and run: $0"
  exit 1
fi

ACTION_ZIP="${ROOT_DIR}/assemblies/assemblies-action-ilivalidator/target/hop-action-ilivalidator-${VERSION}.zip"
TRANSFORM_ZIP="${ROOT_DIR}/assemblies/assemblies-transform-ilivalidator/target/hop-transform-ilivalidator-${VERSION}.zip"

if [[ ! -f "${ACTION_ZIP}" || ! -f "${TRANSFORM_ZIP}" ]]; then
  echo "Assembly ZIPs not found."
  echo "Build them first with:"
  echo "  mvn -pl assemblies/assemblies-action-ilivalidator,assemblies/assemblies-transform-ilivalidator -am package"
  exit 1
fi

mkdir -p "${HOP_HOME}"
unzip -o "${ACTION_ZIP}" -d "${HOP_HOME}" >/dev/null
unzip -o "${TRANSFORM_ZIP}" -d "${HOP_HOME}" >/dev/null

echo "Installed INTERLIS validator plugins into: ${HOP_HOME}"
echo "  - plugins/actions/ilivalidator"
echo "  - plugins/transforms/ilivalidator"
