#!/bin/bash

echo "starting isc-dhcp-server service"
service isc-dhcp-server restart
#service isc-dhcp-server start
tail -f /dev/null
#echo "[hit enter key to exit] or run 'docker stop <container>'"
#read
