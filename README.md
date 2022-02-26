The "digitalpanda-*|cassandra" roles require to initialize once the node with the digitalpanda-base role.
> ansible-playbook digitalpanda-base.yml --inventory-file=digitalpanda-inventory --ask-become-pass -u=${TARGET_MACHINE_BASE_USER}

The "confluent-*|hadoop-*" roles require to initialize once the node with the cluster-node-base-nuc/pi role.


# Commands
Publish digitalpanda-common to local maven repository
> mvn install -f digitalpanda-common/pom.xml