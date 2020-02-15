#!/bin/bash

echo "starting ddclient (dyndns) service"
service ddclient start &>> /opt/status.txt
tail -f /dev/null
#echo "[hit enter key to exit] or run 'docker stop <container>'"
#read
