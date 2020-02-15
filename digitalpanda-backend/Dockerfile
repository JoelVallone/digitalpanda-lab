FROM openjdk:8u191-alpine
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

ENV TARGET_ENV local
ENV USER_ID 100
ENV GROUP_ID 100
ENV BASE_DIR  /opt/backend
ENV BIN_DIR ${BASE_DIR}/bin

RUN mkdir -p ${BIN_DIR} && \
    chown -R ${USER_ID}:${GROUP_ID} ${BASE_DIR}

WORKDIR ${BASE_DIR}

COPY --chown=100:100 digitalpanda-backend-application/target/backend.application*.jar ${BIN_DIR}/backend.jar

USER ${USER_ID}:${GROUP_ID}
EXPOSE 8081
CMD java -jar -Dspring.profiles.active=container.${TARGET_ENV} ${BIN_DIR}/backend.jar