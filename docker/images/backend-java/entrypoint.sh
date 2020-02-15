#!/bin/bash

echo "Starting jar file"
java -version
java -jar ${BIN_DIR}/backend.jar &> ${LOG_DIR}/digitalpanda-backend.log
echo "End of entrypoint file"



