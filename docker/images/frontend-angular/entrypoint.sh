#!/bin/bash

echo "starting npm server"
http-server -a 0.0.0.0 -p 8000 ${BIN_DIR}/digitalpanda-frontend &> ${LOG_DIR}/digitalpanda-frontend.log
