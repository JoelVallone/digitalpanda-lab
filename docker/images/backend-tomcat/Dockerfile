FROM ubuntu:14.04
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

ENV SHARE_FOLDER /usr/share
ENV JAVA_HEAP_SIZE 512
ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64

RUN   apt-get update && \
  apt-get install -y \
          tomcat7 \ 
	  && \
  rm -rf /var/lib/apt/lists/*

RUN sed -i "s|#JAVA_HOME=.*|JAVA_HOME=$JAVA_HOME|g" /etc/default/tomcat7
RUN sed -i "s|-Xmx128m|-Xmx${JAVA_HEAP_SIZE}m|g" /etc/default/tomcat7

#CMD rm -rf /var/lib/tomcat7/webapps/ROOT
COPY home.war /var/lib/tomcat7/webapps/ROOT.war
COPY entrypoint.sh /opt/entrypoint.sh
CMD echo "<Context docBase="home" path="" reloadable="true" />" > /etc/tomcat7/Catalina/localhost/ROOT.xml

EXPOSE 8080

ENTRYPOINT /opt/entrypoint.sh