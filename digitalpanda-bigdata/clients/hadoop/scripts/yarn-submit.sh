#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
SCRIPT_FOLDER="$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
ROOT_FOLDER="${SCRIPT_FOLDER}/.."
HADOOP_VERSION=2.9.2
HADOOP_NAME=hadoop-${HADOOP_VERSION}
HADOOP_FOLDER=${ROOT_FOLDER}/${HADOOP_NAME}
HADOOP_CLIENT_CONFIG_FOLDER=${ROOT_FOLDER}/config/client-config

if [ ! -d "${ROOT_FOLDER}/${HADOOP_NAME}" ]; then
    echo "DOWNLOAD HADOOP : ${HADOOP_NAME}"
    cd ${ROOT_FOLDER}
    wget https://www-eu.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz
    tar -zxf ${HADOOP_NAME}.tgz
    rm ${HADOOP_NAME}.tgz
    cd ${SCRIPT_FOLDER}
fi

echo "SUBMIT HADOOP JOB"
${HADOOP_FOLDER}/bin/hadoop  \
    --config $HADOOP_CLIENT_CONFIG_FOLDER \
    jar ${HADOOP_FOLDER}/share/hadoop/mapreduce/hadoop-mapreduce-examples-${HADOOP_VERSION}.jar pi 16 1000


