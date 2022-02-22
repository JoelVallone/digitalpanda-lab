The "digitalpanda-*|cassandra" roles require to initialize once the node with the digitalpanda-base role.

The "confluent-*|hadoop-*" roles require to initialize once the node with the cluster-node-base-nuc/pi role.
> ansible-playbook digitalpanda-base.yml --inventory-file=digitalpanda-inventory --ask-become-pass


# Commands
Publish digitalpanda-common to local maven repository
> mvn install -f digitalpanda-common/pom.xml