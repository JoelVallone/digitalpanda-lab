#!/bin/bash
# Push a docker image to the local docker registry
# Author: Joël Vallone, joel.vallone@gmail.com

#Local constants
SCRIPT_NAME=$0
SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
INITIAL_FOLDER=$(pwd)
echo "Build software"
cd ${SCRIPT_FOLDER}/../../digitalpanda-iot/
mvn clean install
cd ${SCRIPT_FOLDER}/../../digitalpanda-backend/
./scripts/build.sh
cd ${SCRIPT_FOLDER}/../../digitalpanda-frontend/
./scripts/build-prod.sh
echo "Build containers"
cd ${SCRIPT_FOLDER}/../../digitalpanda-infrastructure/docker/compose/
#sudo ${SCRIPT_FOLDER}/docker/scripts/push.sh backend-java frontend-angular2
sudo docker-compose -f compose.yml build
echo "Restart containers"
sudo docker-compose -f compose.yml up -d
cd ${INITIAL_FOLDER}
#${SCRIPT_FOLDER}/docker/scripts/deploy.sh digitalpanda-backend-0 digitalpanda-frontend-0

