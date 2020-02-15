#!/bin/bash

echo "starting tomcat"
mkdir ${SHARE_FOLDER}/tomcat7/logs
/etc/init.d/tomcat7 start
/etc/init.d/tomcat7 stop
rm -rf /var/lib/tomcat7/webapps/ROOT
/etc/init.d/tomcat7 start; tail -f /var/log/tomcat7/*



#/usr/share/tomcat7/bin/startup.sh
#service tomcat7 start &>> /opt/status.txt
#echo "opening bash for future inspections"
echo -e "To detatch from the container : hit ctrl+p, then ctrl+q \nTo stop the container run: 'docker stop <container>'"
#/bin/bash 




