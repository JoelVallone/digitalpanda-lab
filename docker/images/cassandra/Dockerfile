FROM ubuntu:16.04
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

ENV JAVA_HOME  /usr/lib/jvm/java-8-openjdk-amd64
ENV JAVA_HEAP_SIZE 2048

ENV CASSANDRA_VERSION 3.11.3

ENV BASE_DIR /opt/cassandra
ENV APP_DIR ${BASE_DIR}/apache-cassandra-${CASSANDRA_VERSION}
ENV CONF_DIR ${BASE_DIR}/conf
ENV LOG_DIR ${BASE_DIR}/logs

ENV DATA_DIR ${BASE_DIR}/data
ENV DATA_DIR_SSTABLES ${DATA_DIR}/datafile
ENV DATA_DIR_COMMITLOG ${DATA_DIR}/commitlog
ENV DATA_DIR_CDC ${DATA_DIR}/cdc
ENV DATA_DIR_SAVED_CACHES ${DATA_DIR}/saved_caches
ENV DATA_DIR_HINTS ${DATA_DIR}/hints


ENV CASSANDRA_AUTO_START false

# Install java 8
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk python net-tools dnsutils wget && \
    rm -rf /var/lib/apt/lists/*

# Install cassandra ${CASSANDRA_VERSION}
RUN mkdir -p ${BASE_DIR} && \
    cd ${BASE_DIR} && \
    wget mirror.easyname.ch/apache/cassandra/${CASSANDRA_VERSION}/apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz && \
    tar -zxf apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz && \
    rm apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz;

# Setup cassandra directories
WORKDIR ${BASE_DIR}
RUN mkdir -p ${DATA_DIR_SSTABLES} && \
    mkdir -p ${DATA_DIR_COMMITLOG} && \
    mkdir -p ${DATA_DIR_CDC} && \
    mkdir -p ${DATA_DIR_SAVED_CACHES} && \
    mkdir -p ${DATA_DIR_HINTS} && \
    mkdir -p ${LOG_DIR}

# Load default settings
COPY entrypoint.sh /opt/entrypoint.sh
COPY default-config ${CONF_DIR}

ENTRYPOINT /opt/entrypoint.sh