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

# Hadoop-client
ENV HADOOP_VERSION=2.8.3
ENV HADOOP_ARCHIVE=hadoop-${HADOOP_VERSION}.tar.gz
ENV HADOOP_FOLDER_NAME=hadoop-${HADOOP_VERSION}
RUN wget http://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/${HADOOP_ARCHIVE} && \
    tar -zxf ${HADOOP_ARCHIVE} && \
    rm ${HADOOP_ARCHIVE};
ENV PATH="${TOOLBOX_BASE}/${HADOOP_FOLDER_NAME}/bin:${TOOLBOX_BASE}/${HADOOP_FOLDER_NAME}/sbin:${PATH}"

# Flink-client
ENV FLINK_VERSION=1.9.2
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
ENV HADOOP_FLINK_BUNDLE_JAR=flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-7.0.jar
ENV FLINK_LIB_FOLDER="${TOOLBOX_BASE}/${FLINK_FOLDER_NAME}/lib"
RUN cd ${FLINK_LIB_FOLDER} && \
    wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/${HADOOP_VERSION}-7.0/${HADOOP_FLINK_BUNDLE_JAR}

# Python 3 environment
RUN apt-get update && \
    apt-get install -y python3 python3-pip python3-dev && \
    rm -rf /var/lib/apt/lists/*

RUN pip3 install --upgrade pip && \
    pip3 install virtualenv pathlib2;

# Jupyter-lab environment setup with special user
ENV USER_ID 1002
ENV USER_NAME panda-worker
RUN useradd -m --uid ${USER_ID} ${USER_NAME}

ENV JUPYTERLAB_BIN_FOLDER=${TOOLBOX_BASE}/juypter-lab
ENV JUPYTERLAB_STATE_FOLDER=${TOOLBOX_STATE}/juypter-lab
ENV JUPYTERLAB_CONFIG_FOLDER=${TOOLBOX_CONFIG}

RUN mkdir -p ${JUPYTERLAB_BIN_FOLDER}    && chown ${USER_ID} ${JUPYTERLAB_BIN_FOLDER} && \
    mkdir -p ${JUPYTERLAB_STATE_FOLDER}  && chown ${USER_ID} ${JUPYTERLAB_STATE_FOLDER} && \
    mkdir -p ${JUPYTERLAB_CONFIG_FOLDER} && chown ${USER_ID} ${JUPYTERLAB_CONFIG_FOLDER}

USER ${USER_ID}

# Jupyter-lab
RUN cd ${JUPYTERLAB_BIN_FOLDER} && \
    virtualenv jupyter-lab-env && \
    . ./jupyter-lab-env/bin/activate && \
    pip install jupyterlab

RUN mkdir -p ${JUPYTERLAB_STATE_FOLDER}  && \
    mkdir -p ${JUPYTERLAB_CONFIG_FOLDER} && \
    . ${JUPYTERLAB_BIN_FOLDER}/jupyter-lab-env/bin/activate && \
    jupyter notebook --generate-config && \
    mv /home/${USER_NAME}/.jupyter/jupyter_notebook_config.py ${JUPYTERLAB_CONFIG_FOLDER}/jupyter_notebook_config.py

CMD . ${JUPYTERLAB_BIN_FOLDER}/jupyter-lab-env/bin/activate; \
    jupyter lab \
        --ip 0.0.0.0 \
        --port 8888 \
        --NotebookApp.notebook_dir="${JUPYTERLAB_STATE_FOLDER}" \
        --NotebookApp.token='' \
        --NotebookApp.password='' \
        --config=${TOOLBOX_CONFIG}/jupyter_notebook_config.py