FROM ubuntu:14.04
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

ENV BASE_DIR /opt/frontend
ENV LOG_DIR ${BASE_DIR}/log
ENV BIN_DIR ${BASE_DIR}/bin

RUN  \
   apt-get update && \
   apt-get install -y curl git

RUN  \
  curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash - && \
  apt-get install -y nodejs && \
  rm -rf /var/lib/apt/lists/*

RUN  \
     npm install http-server -g

#RUN cd /opt && \
#    git clone https://github.com/JoelVallone/digitalpanda-frontend.git   &&  \
#    cd ./digitalpanda-frontend  && \
#    npm install

RUN mkdir -p ${BIN_DIR} && \
    mkdir -p ${LOG_DIR}

WORKDIR ${BASE_DIR}

COPY digitalpanda-frontend ${BIN_DIR}/digitalpanda-frontend
COPY entrypoint.sh /opt/entrypoint.sh

ENTRYPOINT /opt/entrypoint.sh