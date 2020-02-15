#!/bin/bash
set -e

SCRIPTS_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_OUTPUT_FOLDER="${SCRIPTS_FOLDER}/../../digitalpanda-infrastructure/docker/images/backend-java"

echo "=> Build backend"
cd ${SCRIPTS_FOLDER}/../
mvn clean install

echo "=> Copy backend binary to docker image external folder"
rm -f "${JAR_OUTPUT_FOLDER}/"*backend*.jar
cp "${SCRIPTS_FOLDER}/../digitalpanda-backend-application/target/"*backend*.jar "${JAR_OUTPUT_FOLDER}/"

echo "=>Build and push image to registry"
VERSION="1.0.0"
REGISTRY="fanless1.digitalpanda.org:5000"
IMAGE_NAME=${REGISTRY}/digitalpanda-backend:${VERSION}
docker build -t ${IMAGE_NAME} ${SCRIPTS_FOLDER}/../
docker push ${IMAGE_NAME}
