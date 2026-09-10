#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
HOP_DEBUG_DIR="${1:-${HOP_DEBUG_DIR:-${ROOT_DIR}/assemblies/debug/target/hop}}"
if [[ ! -d "${HOP_DEBUG_DIR}" ]]; then
  echo "Create the debug layout first: mvn -Pdebug -pl assemblies/debug -am package"
  exit 1
fi
exec bash "${SCRIPT_DIR}/dev-sync-hop-plugin.sh" "${HOP_DEBUG_DIR}" suite
