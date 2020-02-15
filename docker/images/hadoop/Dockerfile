FROM ubuntu:18.04
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

ENV JAVA_HOME  /usr/lib/jvm/java-8-openjdk-amd64
ENV JAVA_HEAP_SIZE 2048
ENV HADOOP_VERSION "2.8.3"
ENV HADOOP_BASE  /opt/hadoop
ENV HADOOP_PREFIX ${HADOOP_BASE}/hadoop-${HADOOP_VERSION}
ENV HADOOP_CONF_DIR ${HADOOP_BASE}/ext/config 
ENV HADOOP_LOG_DIR ${HADOOP_BASE}/ext/log

# Install java 8
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk wget && \
    rm -rf /var/lib/apt/lists/*

# Install hadoop
RUN mkdir -p ${HADOOP_BASE} && \
    cd ${HADOOP_BASE} && \
    wget http://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz && \
    tar -zxf hadoop-${HADOOP_VERSION}.tar.gz && \
    rm hadoop-${HADOOP_VERSION}.tar.gz;

# Setup hadoop directories
WORKDIR ${HADOOP_BASE}
RUN mkdir -p $HADOOP_BASE/ext/hdfs/master/data && \
    mkdir -p $HADOOP_BASE/ext/hdfs/slave/data && \
    mkdir -p $HADOOP_CONF_DIR && \
    mkdir -p $HADOOP_LOG_DIR

# Load default settings
COPY entrypoint.sh /opt/entrypoint.sh
RUN chmod gu+x /opt/entrypoint.sh
COPY default-config ${HADOOP_CONF_DIR}

ENTRYPOINT ["/opt/entrypoint.sh"]
CMD ["date"]