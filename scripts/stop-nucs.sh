#!/bin/bash
SCRIPT_FOLDER="$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
ANSIBLE_FOLDER="${SCRIPT_FOLDER}/../ansible/"

cd ${ANSIBLE_FOLDER}
rm -rf ~/.ansible
ansible-playbook ${ANSIBLE_FOLDER}/manage.yml --tags="shutdown" --inventory-file=${ANSIBLE_FOLDER}/cluster-inventory -f 10
cd -

ssh panda-config@fanless1 "sudo docker stop cp-schema-registry"

