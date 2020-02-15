#!/bin/bash

echo "starting bind9 service"
service bind9 start
tail -f /dev/null
#echo "[hit enter key to exit] or run 'docker stop <container>'"
#read
