#!/bin/bash

SSH_SERVER=192.168.0.102
SSH_USER=jva

function usage {
    echo "USAGE: $0 [start|stop]"
    exit 1
}

function startTunnelTo {
    echo "Tunnel localhost:${1}->${2}:${1} via ssh server ${SSH_SERVER} // ${3}"
    ssh -fN -L ${1}:${2}:${1} ${SSH_USER}@${SSH_SERVER}  &> /dev/null
}

function startPi32Tunnels {
    NODE_TARGET_IP=192.168.1.102

    #Hadoop HDFS datanode HTTP address
    startTunnelTo 50075 ${NODE_TARGET_IP} "HDFS datanode HTTP server"

    #Hadoop YARN nodemanager HTTP Address
    startTunnelTo 8042 ${NODE_TARGET_IP} "YARN nodemanager HTTP server"
}

function killAllTunnels {
    SSH_TUNNEL_PIDS=$(ps -elf | grep -E ssh.*-L | grep -v grep | awk '{ print $4 }')
    echo "kill all tunnels: ${SSH_TUNNEL_PIDS}"
    kill ${SSH_TUNNEL_PIDS}
}

if [ ! -n $1 ]; then
    usage;
fi

case $1 in
    start)
	startPi32Tunnels
	;;
    stop)
	killAllTunnels
	;;
    *)
	usage
	;;
esac
