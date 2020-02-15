# New stressed-panda setup
#1) OS install: 
## install Ubuntu OS from usb device: 
 - os-image: http://cdimage.ubuntu.com/releases/18.04.3/release/ubuntu-18.04.3-server-amd64.iso
 - setup hostname stressed-panda-{$NODE_ID}

#2) base config
## Setup node wifi connection
- Install network-manager for wifi + pair device to wifi network
    > apt install network-manager
    > service network-manager start
    > nmcli d wifi connect <wifi-name> password <password>
- Setup static mac address to node wifi ip in the router

#2) base packages
 - sudo apt install python

#3) Ansible playbook for maintenance user and network setup
ansible-playbook -i cluster-inventory new-worker-node-nuc-ubuntu.yml -e ansible_password=${NUC_JVA_SUDO_PWD} --ask-become-pass

# Misc
## sensor thermal information & frequency
https://askubuntu.com/questions/15832/how-do-i-get-the-cpu-temperature
sensors; lscpu | grep MHz
