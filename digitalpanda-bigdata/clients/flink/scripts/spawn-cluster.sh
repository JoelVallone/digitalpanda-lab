#!/bin/bash
#COPY THE CONTENT OF THE PARENT FOLDER INTO THE EDGENODE:
# > scp -r flink jva@fanless1:./

set -e

export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
SCRIPT_FOLDER="$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)"
ROOT_FOLDER="${SCRIPT_FOLDER}/.."

HADOOP_VERSION=2.8.3
FLINK_VERSION=1.9.1
SCALA_VERSION=2.11

function docker_run_hadoop {
  DOCKER_RUN_CMD="$1"
  sudo docker run --net=host --rm  fanless1.digitalpanda.org:5000/hadoop:2.8.3 "/opt/hadoop/hadoop-2.8.3/bin/${DOCKER_RUN_CMD}" || true
}

FLINK_ARCHIVE=flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz
FLINK_FOLDER=flink-${FLINK_VERSION}
if [ ! -d "${ROOT_FOLDER}/${FLINK_FOLDER}" ]; then
    echo -e "\n\nDOWNLOAD FLINK : ${HADOOP_NAME}"
    cd ${ROOT_FOLDER}
    wget https://www-eu.apache.org/dist/flink/${FLINK_FOLDER}/${FLINK_ARCHIVE}
    tar -zxf ${FLINK_ARCHIVE}
    rm ${FLINK_ARCHIVE}
    cd ${FLINK_FOLDER}
fi

HADOOP_FLINK_BUNDLE_JAR=flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-7.0.jar
FLINK_LIB_FOLDER="${ROOT_FOLDER}/${FLINK_FOLDER}/lib"
if [ ! -f "${FLINK_LIB_FOLDER}/${HADOOP_FLINK_BUNDLE_JAR}" ]; then
    echo -e "\n\nDOWNLOAD HADOOP BUNDLE FOR FLINK : ${HADOOP_NAME}"
    cd ${FLINK_LIB_FOLDER}
    wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/${HADOOP_VERSION}-7.0/${HADOOP_FLINK_BUNDLE_JAR}
fi

#https://ci.apache.org/projects/flink/flink-docs-release-1.9/ops/deployment/yarn_setup.html
echo -e "\n\nSTART FLINK DETACHED SESSION"
cd "${ROOT_FOLDER}/${FLINK_FOLDER}"
export YARN_CONF_DIR="${ROOT_FOLDER}/config/hadoop"
export HADOOP_CONF_DIR="${ROOT_FOLDER}/config/hadoop"
YARN_KILLS_FLINK=$(./bin/yarn-session.sh -n 2 -s 2 -st -jm 2048m -tm 2048m -d | tail -n 1)
echo "Started flink as YARN application, kill it with: ${YARN_KILLS_FLINK}"

echo -e "\n\nSUBMIT FLINK WORDCOUNT BATCH-JOB"
sudo docker run --net=host --rm  fanless1.digitalpanda.org:5000/hadoop:2.8.3 \
  'wget -O LICENSE-2.0.txt http://www.apache.org/licenses/LICENSE-2.0.txt; /opt/hadoop/hadoop-2.8.3/bin/hadoop fs -copyFromLocal LICENSE-2.0.txt hdfs:///LICENSE-2.0.txt' || true
./bin/flink run ./examples/batch/WordCount.jar \
       --input hdfs:///LICENSE-2.0.txt --output hdfs:///wordcount-result-$(date +%s).txt

echo -e "\n\nKILL FLINK DETACHED SESSION"
docker_run_hadoop "${YARN_KILLS_FLINK}"
docker_run_hadoop "hdfs dfs -rm -r /user/jva/.flink"


