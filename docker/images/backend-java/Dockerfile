FROM ubuntu:14.04
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

ENV BASE_DIR  /opt/backend
ENV LOG_DIR ${BASE_DIR}/log
ENV BIN_DIR ${BASE_DIR}/bin
ENV JAVA_HEAP_SIZE 512

RUN apt-get update && \
    apt-get install -y software-properties-common

RUN add-apt-repository ppa:openjdk-r/ppa && \
 apt-get update && \
 apt-get install -y openjdk-8-jdk && \
 rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${BIN_DIR} && \
    mkdir -p ${LOG_DIR}

WORKDIR ${BASE_DIR}

COPY *backend*.jar ${BIN_DIR}/backend.jar
COPY entrypoint.sh /opt/entrypoint.sh

EXPOSE 8080

ENTRYPOINT /opt/entrypoint.sh