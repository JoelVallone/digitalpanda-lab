FROM eclipse-temurin:11-jdk-alpine
MAINTAINER Joel Vallone <joel.vallone@gmail.com>

ENV APP_DIST_NAME "digitalpanda-backend-play-0.1.0"

ENV TARGET_ENV local
ENV USER_ID 1001
ENV GROUP_ID 1001
ENV BASE_DIR  /opt/backend

RUN mkdir -p ${BASE_DIR} && \
    chown -R ${USER_ID}:${GROUP_ID} ${BASE_DIR}

WORKDIR ${BASE_DIR}

RUN apk add --update bash && rm -rf /var/cache/apk/*

COPY --chown=1001:1001 ${APP_DIST_NAME} ${BASE_DIR}

USER ${USER_ID}:${GROUP_ID}
EXPOSE 9000
CMD ${BASE_DIR}/bin/digitalpanda-backend-play -Dplay.http.secret.key=changeitchangeitchangeitchangeitchangeit