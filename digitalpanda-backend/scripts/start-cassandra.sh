#!/bin/bash

SCRIPT_FOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CASSANDRA_CONTAINER="${SCRIPT_FOLDER}/../../digitalpanda-infrastructure/docker/images/cassandra"

if [ ! -z "$(sudo docker ps | grep cassandra:latest )" ]; then
 echo "Cassandra instance already started"
 exit 0;
fi

echo "Starting Cassandra instance:"
ORIGIN_DIR=$(pwd)
cd ${CASSANDRA_CONTAINER}
sudo docker build -t cassandra:latest .
sudo docker run -d --net=host -t cassandra:latest
cd ${ORIGIN_DIR}
