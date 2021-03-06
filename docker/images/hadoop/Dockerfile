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

# Pyspark on worker nodes : python & libraries must be the same version as the driver
ENV SPARK_VERSION=2.4.5
ENV PYTHON_VERSION=3.4
RUN  apt-get update && \
     apt-get install -y software-properties-common && \
     add-apt-repository -y ppa:deadsnakes/ppa && \
     apt-get update -y && \
     apt-get install -y python${PYTHON_VERSION} python3-pip && \
     rm -rf /var/lib/apt/lists/*

RUN python${PYTHON_VERSION} -m pip install pyspark==${SPARK_VERSION} && \
    rm -rf ~/.cache

ENTRYPOINT ["/opt/entrypoint.sh"]
CMD ["date"]