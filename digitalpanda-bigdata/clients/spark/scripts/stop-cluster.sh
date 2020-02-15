#!/bin/bash
SCRIPT_FOLDER="$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
ANSIBLE_FOLDER=${SCRIPT_FOLDER}/../../ansible

cd ${ANSIBLE_FOLDER}
rm -rf ~/.ansible
ansible-playbook manage.yml --tags="shutdown" --inventory-file=cluster-inventory -f 10
cd -
