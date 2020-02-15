#!/bin/bash

function log {
    echo "$(date) - $1"
}

function startBackgroundCassandra {
    CASSANDRA_CONF=file:///${CONF_DIR}
    export CASSANDRA_CONF

    JVM_OPTS="-Dcassandra.logdir=${LOG_DIR} -Dlogback.configurationFile=${CONF_DIR}/logback.xml"
    export JVM_OPTS
    log "start cassandra in single-node mode"
    ${APP_DIR}/bin/cassandra -R -p ${CONF_DIR}/cassandra.pid &> /dev/null
    echo "cassandra pid:" $(cat  ${CONF_DIR}/cassandra.pid)
}

function stopCassandra {
    pkill -f *cassandra*
    sleep 5
    log "cassandra stop"
}

if [ "${CONTAINER_AUTO_START:-false}" = "True" ]; then
    trap stopCassandra SIGTERM
    trap stopCassandra SIGINT
    startBackgroundCassandra
else
    log "cassandra container started"
fi
tail -f /dev/null
