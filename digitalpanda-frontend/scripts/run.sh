#!/bin/bash

# Run the full digitalpanda stack on the local dev machine
#
# Prerequisite:
# - Ansible role "digitalpanda-base" already initialized the node config
#     See lab's main README.md
# - Docker is installed
# - Docker can be run without sudo by the user running this script
#     See: https://docs.docker.com/engine/install/linux-postinstall/
# - Python 2.7 dependencies to run the ansible-docker module (for panda-config user)
#     > pip2 uninstall urllib3
#     > pip2 install docker requests
#     > rm -rf ~/.local/lib/python2.7/site-packages/requests/packages/urllib3/

set -e

SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ANSIBLE_FOLDER="${SCRIPT_FOLDER}/../../ansible"


function stop_containers() {
    echo "Stopping containers"
    docker stop $(docker ps -q  -f name=cassandra -f name=digitalpanda-backend) &> /dev/null || true
}

if [ $# -gt 0 ] && [ $1 = "-b" ];then
    stop_containers
    trap stop_containers INT
    cd ${ANSIBLE_FOLDER}
    echo "Deploy backend stack with Ansible"
    ansible-playbook digitalpanda-stack.yml \
      --inventory-file=digitalpanda-inventory-local \
      --extra-vars "inject_test_data=true deploy_frontend=false"
    docker ps
    cd -
fi

sleep 5
echo "Contacting backend REST API"
curl -v http://localhost:8081/ui/greeting

echo "Build and serve frontend"
ng serve --disableHostCheck --host=0.0.0.0
