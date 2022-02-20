#!/bin/bash
set -e
set -x

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64;

APP_VERSION="0.1.0"
export APP_DIST_NAME="digitalpanda-backend-play-${APP_VERSION}"

DOCKER_REGISTRY="fanless1.digitalpanda.org:5000"
DOCKER_IMAGE_NAME=${DOCKER_REGISTRY}/digitalpanda-backend-play:${APP_VERSION}

SCRIPTS_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "=> Build backend"
cd ${SCRIPTS_FOLDER}/../
sbt clean universal:packageZipTarball

echo "=> Copy backend binary to docker image external folder"

tar -zxf "${SCRIPTS_FOLDER}/../target/universal/${APP_DIST_NAME}.tgz"
rm -rf "${SCRIPTS_FOLDER}/../target"

echo "=>Build and push image to registry"
sudo docker build -t ${DOCKER_IMAGE_NAME} "${SCRIPTS_FOLDER}/.."
rm -rf "${SCRIPTS_FOLDER}/../${APP_DIST_NAME}"
sudo docker push ${DOCKER_IMAGE_NAME}
