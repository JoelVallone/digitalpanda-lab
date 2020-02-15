#!/bin/bash
# DOC: https://www.tutorialspoint.com/avro/serialization_by_generating_class.htm

SCRIPT_FOLDER="$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
ROOT_FOLDER="${SCRIPT_FOLDER}/.."
AVRO_TOOL_JAR_NAME="avro-tools-1.9.1.jar"
AVRO_TOOL_JAR_PATH="${SCRIPT_FOLDER}/${AVRO_TOOL_JAR_NAME}"
SCHEMA_FOLDER="${SCRIPT_FOLDER}/avro-schemas"
DEST_CODE_FOLDER="${ROOT_FOLDER}/src/main/java/"

if [ ! -f "${AVRO_TOOL_JAR_PATH}" ]; then
    echo "DOWNLOAD avro-tools jar: ${AVRO_TOOL_JAR_NAME}"
    cd ${SCRIPT_FOLDER}
    wget https://repo1.maven.org/maven2/org/apache/avro/avro-tools/1.9.1/${AVRO_TOOL_JAR_NAME}
fi

echo "Generate avro .java classes into ${DEST_CODE_FOLDER}"
java -jar ${AVRO_TOOL_JAR_PATH} compile schema "${SCHEMA_FOLDER}" "${DEST_CODE_FOLDER}"


