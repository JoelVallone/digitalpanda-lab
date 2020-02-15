#!/bin/bash
# Push a docker image to the local docker registry
# Author: Joël Vallone, joel.vallone@gmail.com

#Local constants
SCRIPT_NAME=$0
SCRIPTS_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "${SCRIPTS_FOLDER}/common.sh"


#local functions
function printUsage() {
    local folders=$(cd ${DOCKER_IMAGE_FOLDER}; ls -d */)
    folders="${folders//\/}"
    errorMessage  "USAGE: $SCRIPT_NAME {space separated container names OR \"all\"} \n\
POSSIBLE IMAGES : \n\
all \n\
${folders}"
}

function pushToRegistry() {
    local imageName=${1}
    local fullImageName="${DOCKER_REGISTRY_FQDN}:${DOCKER_REGISTRY_PORT}/${DOCKER_REGISTRY_USERNAME}/${imageName}"
    local imageFullPath="${DOCKER_IMAGE_FOLDER}/${imageName}"
    echo "Build and push docker image \"${imageName}\" to docker registry ${DOCKER_REGISTRY_FQDN}:${DOCKER_REGISTRY_PORT}"
    
    cmd="sudo docker build -t ${fullImageName} ${imageFullPath}"
    echo ${cmd}
    eval ${cmd}

    cmd="sudo docker push ${fullImageName}"
    echo ${cmd}
    eval ${cmd}
}

#fetch and check input
if [ $# -lt 1 ]; then
    printUsage
fi

for dockerImageName in $@; do
    dockerImagePath="${DOCKER_IMAGE_FOLDER}/${dockerImageName}"
    allStr=$(echo ${dockerImageName} | awk '{print toupper($0)}')
    if [ -d  "${dockerImagePath}" ]; then
	pushToRegistry ${dockerImageName} "${dockerImagePath}"
    elif [  ${allStr} = "ALL" ]; then
	dockerImagesName="$(ls ${DOCKER_IMAGE_FOLDER})"
	echo "push all images in ${DOCKER_IMAGE_FOLDER}"
	for dockerImageName in ${dockerImagesName}; do
	    dockerImagePath="${DOCKER_IMAGE_FOLDER}/${dockerImageName}"
	    pushToRegistry ${dockerImageName} "${dockerImagePath}"
	done
	break
    else
	errorMessage "The docker image to push \"${dockerImageName}\" is not present in ${DOCKER_IMAGE_FOLDER}"
    fi;
done


    
