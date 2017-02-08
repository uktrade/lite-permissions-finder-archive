FROM openjdk:8-jre

ARG NEXUS_BASE_URL=http://nexus.mgmt.licensing.service.trade.gov.uk.test/repository
ARG NEXUS_REPO=raw-test
ARG CRYPTO_SECRET=abcdefghijk
ARG BUILD_VERSION

ENV ARTEFACT_NAME lite-permissions-finder-$BUILD_VERSION
ENV CONFIG_FILE /conf/permissions-finder-config.conf

LABEL uk.gov.bis.lite.permissions-finder.version=$BUILD_VERSION

WORKDIR /opt/permissions-finder

ADD $NEXUS_BASE_URL/$NEXUS_REPO/lite-permissions-finder/lite-permissions-finder/$BUILD_VERSION/$ARTEFACT_NAME.zip $ARTEFACT_NAME.zip
RUN unzip ${ARTEFACT_NAME}.zip

EXPOSE 9000

CMD $ARTEFACT_NAME/bin/lite-permissions-finder -Dplay.crypto.secret=$CRYPTO_SECRET -Dconfig.file=$CONFIG_FILE
