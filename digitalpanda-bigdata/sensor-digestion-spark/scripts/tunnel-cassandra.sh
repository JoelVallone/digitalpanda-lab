#!/bin/bash

SSH_SERVER_PORT=27002
SSH_SERVER=digitalpanda.org
SSH_USER=jva

function usage {
    echo "USAGE: $0 [start|stop]"
    exit 1
}

function startTunnelTo {
    echo "Tunnel localhost:${1}->${2}:${3} via ssh server ${SSH_SERVER} // ${4}"
    ssh -p ${SSH_SERVER_PORT} -fN -L ${1}:${2}:${3} ${SSH_USER}@${SSH_SERVER}  &> /dev/null
}

function startCassandraTunnel {
    LOCAL_PORT=9040
    NODE_TARGET_IP=192.168.0.102
    NODE_TARGET_PORT=9042

    startTunnelTo ${LOCAL_PORT} ${NODE_TARGET_IP} ${NODE_TARGET_PORT} "Cassandra client"
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
        startCassandraTunnel
        ;;
    stop)
        killAllTunnels
        ;;
    *)
        usage
        ;;
esac
