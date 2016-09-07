FROM java:8

ENV SERVICE_DIR /opt/permissions-finder
ENV ARTEFACT_NAME lite-permissions-finder-1.0-SNAPSHOT

RUN mkdir -p $SERVICE_DIR

COPY ./target/universal/${ARTEFACT_NAME}.zip $SERVICE_DIR

WORKDIR $SERVICE_DIR

RUN unzip ${ARTEFACT_NAME}.zip

CMD ${ARTEFACT_NAME}/bin/lite-permissions-finder -Dplay.crypto.secret=abcdefghijk
