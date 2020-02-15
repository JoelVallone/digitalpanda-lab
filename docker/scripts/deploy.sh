#!/bin/bash
# (Re)deploy all the docker containers
# Author: Joël Vallone, joel.vallone@gmail.com

#Local constants
SCRIPT_NAME=$0
INSTANCE_NAME=0
DOCKER_IMAGE=1
SERVER_MODE=2
BINDING_IP=3
BINDING_PORT=4
SSH_IP=5
SSH_PORT=6
SCRIPTS_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "${SCRIPTS_FOLDER}/common.sh"

#local functions
function printUsage() {
    errorMessage  "USAGE: $SCRIPT_NAME {space separated container names OR \"all\"} \n\
POSSIBLE CONTAINER NAMES: \n\
$(echoContainerList)"
}

function echoContainerList(){
    local srvId=0;
    local text="";
    while [ ${srvId} -lt ${serviceCnt} ]; do
	configLine=${serviceConfigArr[${srvId}]};
	srvConfig=(${configLine//,/ });
	text=${text}"${srvConfig[${INSTANCE_NAME}]}\n";
	srvId=$((srvId + 1));
    done;
    echo -e ${text}

}
function fetchCsvConfigFile(){
    serviceCnt=0
    first=1
    while read line ;do
	if [ ${first} -eq 1 ]; then first=0; continue; fi
	serviceConfigArr[${serviceCnt}]=${line}
	serviceCnt=$((serviceCnt + 1))
    done < ${SERVER_CONFIG_FILE_PATH}
}

function echoCsvConfigFromInstanceName(){
  if [ ! -z ${1+x} ]; then
	srvId=-1;
	instance='-';
	while [ ${instance} != ${1} -a ${srvId} -lt ${serviceCnt} ]; do
	    configLine=${serviceConfigArr[${srvId}]};
	    srvConfig=(${configLine//,/ });
	    instance=${srvConfig[${INSTANCE_NAME}]};
	    srvId=$((srvId + 1));
	done
	if [ ${srvId} -lt ${serviceCnt} ]; then
	    echo ${configLine};
	else
	    errorMessage "${SCRIPT_NAME}.getCsvConfig(): unknown container instance name \"${1}\", please check your server config file at ${SCRIPTS_FOLDER}/${SERVER_CONFIG_FILE_PATH}"
	fi
  else
      errorMessage "${SCRIPT_NAME}.getCsvConfig(): missing container name"
  fi
}

function setServiceConfig() {
    srvConfig=(${1//,/ });
    instanceName=${srvConfig[${INSTANCE_NAME}]};
    dockerImage=${srvConfig[${DOCKER_IMAGE}]};
    dockerFullImageName="${DOCKER_REGISTRY_IP}:${DOCKER_REGISTRY_PORT}/${DOCKER_REGISTRY_USERNAME}/${dockerImage}"
    sshIp=${srvConfig[${SSH_IP}]};
    sshPort=${srvConfig[${SSH_PORT}]};
    serverMode=${srvConfig[${SERVER_MODE}]};
    if [ -z ${instanceName} -o -z ${dockerImage} -o -z ${sshIp} -o -z ${sshPort} ]; then
	errorMessage "${SCRIPT_NAME}.startContainer(): \
malformed container csvConfig line for one of the following parameters :\
 -> instance-name=\"${instanceName}\" \n\
 -> docker-image=\"${dockerImage}\" \n\
 -> server-mode=\"${serverMode}\" \n\
 -> ssh-ip=\"${sshIp}\" \n\
 -> ssh-port=\"${sshPort}\" \n\
";     fi
    if [ ${serverMode} != "0" ]; then
	bindingIp=${srvConfig[${BINDING_IP}]};
	bindingPort=${srvConfig[${BINDING_PORT}]};

	if [ -z ${bindingPort} -o -z ${bindingIp} ]; then
	    errorMessage "${SCRIPT_NAME}.startContainer(): \
malformed container csvConfig line for one of the following parameters :\
 -> instance-name=\"${instanceName}\" \n\
 -> binding-ip=\"${bindingIp}\" \n\
 -> binding-port=\"${bindingPort}\" \n\
";	   fi
	msgSrv="with service listening on ${bindingIp}:${bindingPort}"
    else
	bindingIp="";
	bindingPort="";
	msgSrv=""
    fi
    echo -e "\n\nDeploy INSTANCE=${instanceName}  IMAGE=${dockerFullImageName} on ${sshIp} " ${msgSrv};
}

function stopAndRemoveContainer() {

    echo "-> Stop and remove container";
    cmd='containerIds=$(docker ps -q -a --filter name='${instanceName}') && [ ! -z ${containerIds} ] && ( docker stop ${containerIds} ; docker rm ${containerIds})'
    sshRun ${sshPort} ${sshIp} "${cmd}"
}

function startContainer() {
    echo "-> Starting container";
    cmd="docker pull ${dockerFullImageName}; docker run -d --name ${instanceName} "
    if [ ${serverMode} == "1" ]; then
	cmd=${cmd}"-p ${bindingIp}:${bindingPort}:${bindingPort} "
    fi
    cmd=${cmd}"${dockerFullImageName}"
    sshRun ${sshPort} ${sshIp} "${cmd}"
}

function deployContainer(){
    if [ -z ${1} ];then errorMessage "${SCRIPT_NAME}.deployContainer(): no config data" ;fi
    setServiceConfig ${1}
    stopAndRemoveContainer
    startContainer
}

#fetch containers deployment config from file
fetchCsvConfigFile

#fetch and check input
if [ $# -lt 1 ]; then
    printUsage
fi

#stop->remove-deploy each container name defined by user input
for dockerInstanceName in $@; do
    if [ ${dockerInstanceName,,} = "all" ];then
	for ((serviceId = 0; serviceId <= ${serviceCnt} - 1; serviceId++))
	do
	    configLine=${serviceConfigArr[${serviceId}]};
	    deployContainer ${configLine}
	done
	exit 0;
    else
	configLine=$(echoCsvConfigFromInstanceName ${dockerInstanceName});
	deployContainer ${configLine}
    fi
done

