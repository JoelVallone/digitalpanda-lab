#!/bin/bash
set -e

export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64;

SCRIPTS_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_OUTPUT_FOLDER="${SCRIPTS_FOLDER}/../../docker/images/backend-java"

echo "=> Build backend"
cd ${SCRIPTS_FOLDER}/../
mvn clean install

echo "=> Copy backend binary to docker image external folder"
rm -f "${JAR_OUTPUT_FOLDER}/"*backend*.jar
cp "${SCRIPTS_FOLDER}/../target/"*backend*.jar "${JAR_OUTPUT_FOLDER}/"

echo "=>Build and push image to registry"
VERSION="1.2.0"
REGISTRY="fanless1.digitalpanda.org:5000"
IMAGE_NAME=${REGISTRY}/digitalpanda-backend:${VERSION}
sudo docker build -t ${IMAGE_NAME} ${SCRIPTS_FOLDER}/../
sudo docker push ${IMAGE_NAME}
