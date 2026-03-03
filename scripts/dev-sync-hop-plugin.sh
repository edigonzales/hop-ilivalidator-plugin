#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "Usage: $0 <HOP_HOME> [target]"
  echo "Targets: suite (default), transform, action"
  exit 1
fi

HOP_HOME="$1"
TARGET="${2:-suite}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
VERSION="$(sed -n 's|.*<version>\(.*\)</version>.*|\1|p' "${ROOT_DIR}/pom.xml" | head -n 1)"

case "${TARGET}" in
  transform)
    BUILD_MODULES="hop-transform-ilivalidator,assemblies/assemblies-transform-ilivalidator"
    ZIP_PATHS=(
      "${ROOT_DIR}/assemblies/assemblies-transform-ilivalidator/target/hop-transform-ilivalidator-${VERSION}.zip"
    )
    CLEAN_DIRS=(
      "${HOP_HOME}/plugins/transforms/ilivalidator"
    )
    INSTALLED_PATHS=(
      "${HOP_HOME}/plugins/transforms/ilivalidator"
    )
    ;;
  action)
    BUILD_MODULES="hop-action-ilivalidator,assemblies/assemblies-action-ilivalidator"
    ZIP_PATHS=(
      "${ROOT_DIR}/assemblies/assemblies-action-ilivalidator/target/hop-action-ilivalidator-${VERSION}.zip"
    )
    CLEAN_DIRS=(
      "${HOP_HOME}/plugins/actions/ilivalidator"
    )
    INSTALLED_PATHS=(
      "${HOP_HOME}/plugins/actions/ilivalidator"
    )
    ;;
  suite)
    BUILD_MODULES="hop-action-ilivalidator,hop-transform-ilivalidator,assemblies/assemblies-action-ilivalidator,assemblies/assemblies-transform-ilivalidator"
    ZIP_PATHS=(
      "${ROOT_DIR}/assemblies/assemblies-action-ilivalidator/target/hop-action-ilivalidator-${VERSION}.zip"
      "${ROOT_DIR}/assemblies/assemblies-transform-ilivalidator/target/hop-transform-ilivalidator-${VERSION}.zip"
    )
    CLEAN_DIRS=(
      "${HOP_HOME}/plugins/actions/ilivalidator"
      "${HOP_HOME}/plugins/transforms/ilivalidator"
    )
    INSTALLED_PATHS=(
      "${HOP_HOME}/plugins/actions/ilivalidator"
      "${HOP_HOME}/plugins/transforms/ilivalidator"
    )
    ;;
  *)
    echo "Unsupported target: ${TARGET}"
    echo "Supported targets: suite, transform, action"
    exit 1
    ;;
esac

(
  cd "${ROOT_DIR}"
  mvn -pl "${BUILD_MODULES}" -am -DskipTests package
)

mkdir -p "${HOP_HOME}"
for dir in "${CLEAN_DIRS[@]}"; do
  rm -rf "${dir}"
done

for zip_path in "${ZIP_PATHS[@]}"; do
  if [[ ! -f "${zip_path}" ]]; then
    echo "ZIP not found: ${zip_path}"
    exit 1
  fi
  unzip -q -o "${zip_path}" -d "${HOP_HOME}"
done

echo "Installed ${TARGET} into ${HOP_HOME}"
echo "Plugin path(s): ${INSTALLED_PATHS[*]}"
