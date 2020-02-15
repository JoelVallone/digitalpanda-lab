FROM node:10.13-alpine
LABEL mainatiner "Joel Vallone <joel.vallone@gmail.com>"

ENV USER_ID 100
ENV GROUP_ID 100

ENV BASE_DIR /opt/frontend
ENV BIN_DIR ${BASE_DIR}/bin

RUN  npm install http-server -g

RUN mkdir -p ${BIN_DIR} && \
    chown -R ${USER_ID}:${GROUP_ID} ${BASE_DIR}

WORKDIR ${BASE_DIR}

COPY --chown=100:100 dist/digitalpanda ${BIN_DIR}/digitalpanda-frontend/

USER ${USER_ID}:${GROUP_ID}
CMD http-server -a 0.0.0.0 -p 8000 ${BIN_DIR}/digitalpanda-frontend
