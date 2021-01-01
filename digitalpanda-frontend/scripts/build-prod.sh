#!/bin/bash

set -e

SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SRC_FOLDER="${SCRIPT_FOLDER}/../src"
PROD_FOLDER="${SCRIPT_FOLDER}/../dist/digitalpanda"
DOCKER_IMAGE_BIN_FOLDER="${SCRIPT_FOLDER}/../../docker/images/frontend-angular/digitalpanda-frontend"

echo "=> Build frontend"
rm -rf ${PROD_FOLDER}
npm install
# https://update.angular.io/
# ng update @angular/cli @angular/core
npm run ng build -- --prod

echo "=> Copy frontend data to docker image external folder"
rm -rf ${DOCKER_IMAGE_BIN_FOLDER}
cp -r ${PROD_FOLDER} ${DOCKER_IMAGE_BIN_FOLDER}

echo "Build & push image to registry"
VERSION="1.2.1"
REGISTRY="fanless1.digitalpanda.org:5000"
IMAGE_NAME=${REGISTRY}/digitalpanda-frontend:${VERSION}
sudo docker build -t ${IMAGE_NAME} ${SCRIPT_FOLDER}/../
sudo docker push ${IMAGE_NAME}
