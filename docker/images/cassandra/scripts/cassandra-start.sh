#!/bin/bash
#Use only for local dev (non-docker) purpose

BASE_DIR=/opt/cassandra
SCRIPT_DIR="$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
CASSANDRA_DIR=${BASE_DIR}/apache-cassandra-3.11.3
LOG_DIR=${BASE_DIR}/logs
LOG_FILE=${LOG_DIR}/cassandra.log
CONF_DIR=${BASE_DIR}/conf

#Cassandra data directories
DATA_FILE_DIR=${BASE_DIR}/ext/data/datafile
CDC_DIR=${BASE_DIR}/ext/data/cdc
COMMITLOG_DIR=${BASE_DIR}/ext/data/commitlog

mkdir -p ${CONF_DIR}  ${LOG_DIR} ${DATA_FILE_DIR} ${CDC_DIR} ${COMMITLOG_DIR}
cp ${SCRIPT_DIR}/../default-config/* ${CONF_DIR}
#cd ${BASE_DIR}
#wget http://mirror.switch.ch/mirror/apache/dist/cassandra/3.11.3/apache-cassandra-3.11.3-bin.tar.gz && \
#tar -zxf apache-cassandra-3.11.3-bin.tar.gz

CASSANDRA_CONF=file:///${CONF_DIR}
export CASSANDRA_CONF

JVM_OPTS="-Dcassandra.logdir=${LOG_DIR} -Dlogback.configurationFile=${CONF_DIR}/logback.xml"
export JVM_OPTS

${CASSANDRA_DIR}/bin/cassandra -p ${CONF_DIR}/cassandra.pid &> ${LOG_FILE}
tail -f ${LOG_FILE}



