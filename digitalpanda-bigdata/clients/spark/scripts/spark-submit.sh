#!/bin/bash
SCRIPT_FOLDER="$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
ROOT_FOLDER="${SCRIPT_FOLDER}/.."
SPARK_VERSION=spark-2.4.4
SPARK_NAME=${SPARK_VERSION}-bin-hadoop2.7
SPARK_FOLDER=${ROOT_FOLDER}/${SPARK_NAME}
HADOOP_CLIENT_CONFIG_FOLDER=${ROOT_FOLDER}/config/client-config

if [ ! -d "../${SPARK_NAME}" ]; then
    echo "DOWNLOAD SPARK : ${SPARK_NAME}"
    cd ${ROOT_FOLDER}
    wget https://www-eu.apache.org/dist/spark/${SPARK_VERSION}/${SPARK_NAME}.tgz
    tar -zxf ${SPARK_NAME}.tgz
    rm ${SPARK_NAME}.tgz
    cd ${SCRIPT_FOLDER}
fi

echo "SUBMIT SPARK JOB : job 'SparkPi', client mode, on yarn"
export HADOOP_CONF_DIR=${HADOOP_CLIENT_CONFIG_FOLDER}
export YARN_CONF_DIR=${HADOOP_CLIENT_CONFIG_FOLDER}
${SPARK_FOLDER}/bin/spark-submit \
    --class org.apache.spark.examples.SparkPi \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 512m  \
    --executor-memory 512m \
    --executor-cores 10 \
    --queue default  \
    ${SPARK_FOLDER}/examples/jars/spark-examples*.jar 1


