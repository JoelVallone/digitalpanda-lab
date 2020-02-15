FROM ubuntu:16.04
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

RUN  apt-get update && \
     apt-get install -y  bind9 bind9utils bind9-doc dnsutils && \
     rm -rf /var/lib/apt/lists/*

ADD bind /etc/bind

COPY entrypoint.sh /opt/entrypoint.sh

ENTRYPOINT /opt/entrypoint.sh

CMD /bin/bash