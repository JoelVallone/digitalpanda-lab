# Confluent-kafka base image with client
FROM confluentinc/cp-kafka:5.3.1

MAINTAINER Joel Vallone <joel.vallone@gmail.com>
ENV JAVA_HOME  /usr/lib/jvm/zulu-8-amd64

# Base directories
ENV TOOLBOX_BASE  /opt/panda-toolbox
ENV TOOLBOX_CONFIG ${TOOLBOX_BASE}/ext/config
ENV TOOLBOX_STATE ${TOOLBOX_BASE}/ext/state
RUN mkdir -p ${TOOLBOX_BASE}
WORKDIR ${TOOLBOX_BASE}

# Misc. tooling from apt
RUN apt-get update && \
    apt-get install -y wget && \
    rm -rf /var/lib/apt/lists/*

# Cassandra with client
ENV CASSANDRA_VERSION=3.11.3
ENV CASSANDRA_ARCHIVE=apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz
ENV CASSANDRA_FOLDER_NAME=apache-cassandra-${CASSANDRA_VERSION}
RUN wget https://archive.apache.org/dist/cassandra/${CASSANDRA_VERSION}/${CASSANDRA_ARCHIVE} && \
    tar -zxf ${CASSANDRA_ARCHIVE} && \
    rm ${CASSANDRA_ARCHIVE};
RUN ln -s ${TOOLBOX_BASE}/${CASSANDRA_FOLDER_NAME}/bin/cqlsh /usr/bin/cql
ENV PATH="${TOOLBOX_BASE}/${CASSANDRA_FOLDER_NAME}/bin:${PATH}"

# Flink-client
ENV FLINK_VERSION=1.9.1
ENV SCALA_VERSION=2.11
ENV FLINK_ARCHIVE=flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz
ENV FLINK_FOLDER_NAME=flink-${FLINK_VERSION}
RUN wget https://archive.apache.org/dist/flink/${FLINK_FOLDER_NAME}/${FLINK_ARCHIVE}  && \
    tar -zxf ${FLINK_ARCHIVE}  && \
    rm ${FLINK_ARCHIVE}
ENV PATH="${TOOLBOX_BASE}/${FLINK_FOLDER_NAME}/bin:${PATH}"

# Flink-client - Haddoop library
ENV YARN_CONF_DIR="${TOOLBOX_CONFIG}/hadoop"
ENV HADOOP_CONF_DIR="${TOOLBOX_CONFIG}/hadoop"
ENV HADOOP_VERSION=2.8.3
ENV HADOOP_FLINK_BUNDLE_JAR=flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-7.0.jar
ENV FLINK_LIB_FOLDER="${TOOLBOX_BASE}/${FLINK_FOLDER_NAME}/lib"
RUN cd ${FLINK_LIB_FOLDER} && \
    wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/${HADOOP_VERSION}-7.0/${HADOOP_FLINK_BUNDLE_JAR}

CMD ["date"]