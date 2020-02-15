#!/bin/bash
set -e

SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ANSIBLE_FOLDER="${SCRIPT_FOLDER}/../../digitalpanda-infrastructure/ansible"


function stop_containers() {
    echo "Stopping containers"
    sudo docker stop $(sudo docker ps -q  -f name=cassandra -f name=digitalpanda-backend)  || true
}

if [ $# -gt 0 ] && [ $1 = "-b" ];then
    stop_containers
    trap stop_containers INT
    cd ${ANSIBLE_FOLDER}
    echo "Deploy backend stack with Ansible"
    ansible-playbook digitalpanda-stack.yml \
      --inventory-file=digitalpanda-inventory-local \
      --extra-vars "build_code=false clear_state=false inject_test_data=true"
    sudo docker ps
    cd -
fi

sleep 5
echo "Contacting backend REST API"
curl -v http://localhost:8081/ui/greeting

echo "Build and serve frontend"
ng serve --disableHostCheck --host=0.0.0.0
