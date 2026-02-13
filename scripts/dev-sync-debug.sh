#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
VERSION="$(sed -n 's|.*<version>\(.*\)</version>.*|\1|p' "${ROOT_DIR}/pom.xml" | head -n 1)"
HOP_DEBUG_DIR="${1:-${HOP_DEBUG_DIR:-${ROOT_DIR}/assemblies/debug/target/hop}}"

if [[ ! -d "${HOP_DEBUG_DIR}" ]]; then
  echo "Debug Hop layout not found at: ${HOP_DEBUG_DIR}"
  echo "Create it once with:"
  echo "  mvn -pl assemblies/debug -am -DskipTests package"
  exit 1
fi

echo "Building core + plugins (skip tests)..."
mvn -pl ilivalidator-core,hop-action-ilivalidator,hop-transform-ilivalidator -am -DskipTests package

ACTION_PLUGIN_DIR="${HOP_DEBUG_DIR}/plugins/actions/ilivalidator"
TRANSFORM_PLUGIN_DIR="${HOP_DEBUG_DIR}/plugins/transforms/ilivalidator"

mkdir -p "${ACTION_PLUGIN_DIR}" "${TRANSFORM_PLUGIN_DIR}"

rm -f "${ACTION_PLUGIN_DIR}/hop-action-ilivalidator-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/hop-transform-ilivalidator-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/ilivalidator-core-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/ilivalidator-core-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/ilivalidator-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/ilivalidator-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/ili2c-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/ili2c-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/iox-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/iox-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/antlr-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/antlr-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/jaxb-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/jaxb-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/activation-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/activation-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/javax.activation-api-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/javax.activation-api-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/ehibasics-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/ehibasics-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/base64-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/base64-"*.jar
rm -f "${ACTION_PLUGIN_DIR}/jts-core-"*.jar
rm -f "${TRANSFORM_PLUGIN_DIR}/jts-core-"*.jar

cp -f "${ROOT_DIR}/hop-action-ilivalidator/target/hop-action-ilivalidator-${VERSION}.jar" "${ACTION_PLUGIN_DIR}/"
cp -f "${ROOT_DIR}/hop-transform-ilivalidator/target/hop-transform-ilivalidator-${VERSION}.jar" "${TRANSFORM_PLUGIN_DIR}/"

echo "Synchronized plugin jars into: ${HOP_DEBUG_DIR}"
echo "Restart Hop GUI to pick up class changes."
