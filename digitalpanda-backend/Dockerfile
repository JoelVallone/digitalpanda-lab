FROM openjdk:8u191-alpine
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

ENV TARGET_ENV local
ENV USER_ID 1002
ENV GROUP_ID 1002
ENV BASE_DIR  /opt/backend
ENV BIN_DIR ${BASE_DIR}/bin

RUN mkdir -p ${BIN_DIR} && \
    chown -R ${USER_ID}:${GROUP_ID} ${BASE_DIR}

WORKDIR ${BASE_DIR}

COPY --chown=1002:1002 target/digitalpanda-backend*.jar ${BIN_DIR}/backend.jar

USER ${USER_ID}:${GROUP_ID}
EXPOSE 8081
CMD java -jar -Dspring.profiles.active=container.${TARGET_ENV} ${BIN_DIR}/backend.jar