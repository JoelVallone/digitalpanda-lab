#!/bin/bash

RUNNING_CASSANDRA_INSTANCE_ID=$(sudo docker ps -q  --filter=ancestor=cassandra:latest)
[ ! -z ${RUNNING_CASSANDRA_INSTANCE_ID} ] && sudo docker stop ${RUNNING_CASSANDRA_INSTANCE_ID}