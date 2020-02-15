FROM ubuntu:16.04
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

ENV RUNLEVEL=1
ARG DEBIAN_FRONTEND=noninteractive

#RUN sed -i "s/^exit 101$/exit 0/" /usr/sbin/policy-rc.d
RUN apt-get -y update && apt-get -y install rsyslog net-tools isc-dhcp-server  || exit 0

COPY dhcpd.conf /etc/dhcp/dhcpd.conf
COPY entrypoint.sh /opt/entrypoint.sh

ENTRYPOINT /opt/entrypoint.sh
CMD /bin/bash
