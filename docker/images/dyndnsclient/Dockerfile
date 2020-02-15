FROM ubuntu
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

RUN export DEBIAN_FRONTEND=noninteractive && \
  apt-get update && \
  apt-get install -y  \
	  perl \
	  libio-socket-ssl-perl \
	  ddclient \
	  && \
  rm -rf /var/lib/apt/lists/*

COPY ddclient.conf /etc/ddclient.conf
COPY entrypoint.sh /opt/entrypoint.sh

ENTRYPOINT /opt/entrypoint.sh
CMD /bin/bash