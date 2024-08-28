#
# COPYRIGHT Ericsson 2021
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#


# ---------- Runtime docker file --------------------------------------------------
# The docker file works with multiple layers, the gui is built in the 1st layer
# with the required build tools and dependencies, and then the ouput is copied to the main runtime environment
# with only the neccesary runtime dependencies.
#
# ---------- Local testing --------------------------------------------------
# To start the image build it then start it. The internal 3000 port is forwarded to localhost:3000.
#
# docker build -t eric-oss-common-topology-ui -f docker/Dockerfile .
# docker run -d -p 3000:3000 eric-oss-common-topology-ui

# ---------- Params --------------------------------------------------
# Main parameters for the image. If a new 3pp is required the exact version is better to be a parameter.

# Base OS related params. Base OS in ADP is SLES.
ARG COMMON_BASE_OS_DOCKER_URL=armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles
ARG NODEJS_BUILDER_VERSION=16.16.0-1
ARG NODEJS_BUILDER_URL=armdocker.rnd.ericsson.se/proj-adp-cicd-drop/adp-nodejs-lts-builder-image
ARG BASE_OS_REPO=arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/

# ---------- Layer 1 --------------------------------------------------
# Builder: EUI client (brand2)
#   Copy the GUI related files and call build.
#   It is optimized for quick rebuild as the npm install and the project build is separated
FROM $NODEJS_BUILDER_URL:$NODEJS_BUILDER_VERSION as builder_eui_client

# Copy the npm project files first, as these are changed rarely
COPY eric-oss-app-ui/package.json package.json
COPY eric-oss-app-ui/.npmrc .npmrc
RUN npm run install:all

# Now copy the remaining files which can change frequently
# Call the GUI build which creates the runtime artifacts
COPY eric-oss-app-ui/ .
RUN npm run build:dev

# ---------- Layer 1 --------------------------------------------------
# Runtime: Apache Server
FROM $COMMON_BASE_OS_DOCKER_URL:5.11.0-10
ARG BASE_OS_REPO
ARG BASE_OS_VERSION
#Install Apache server
RUN zypper ar -C -G -f https://${BASE_OS_REPO}5.8.0-21 LDC-CBO-SLES \
    && zypper ref -f -r LDC-CBO-SLES \
    && zypper install -l -y shadow \
    && zypper --non-interactive in apache2 \
    && zypper clean all \
    && rm -rf /var/cache/zypp \
    && zypper clean -a
# Other params

ENV APP_DIR /app
WORKDIR ${APP_DIR}

#Add user_id - id is generated as per ADP security guidelines
ARG USER_ID=264522
#In openshift USER_NAME should be numeric and same as user id
ARG USER_NAME=264522

RUN chown -R ${USER_NAME}:0 ${APP_DIR} \
    && chmod -R g=u ${APP_DIR}
RUN echo "${USER_ID}:x:${USER_ID}:0:An Identity for ${USER_NAME}:/nonexistent:/bin/false" >>/etc/passwd
RUN echo "${USER_ID}:!::0:::::" >>/etc/shadow

# Copy UI
COPY --chown=264522:0 --from=builder_eui_client build/ /srv/www/htdocs/eric-oss-app-ui
COPY --chown=264522:0 docker/httpd.conf /tmp/apache2/httpd.conf
COPY --chown=264522:0 docker/entrypoint.sh /usr/sbin/entrypoint.sh

# Configure entrypoint
RUN chmod +x /usr/sbin/entrypoint.sh

RUN chown -R ${USER_NAME}:0 /srv/www/htdocs/ /etc/apache2/ /usr/sbin/*apache* /var/log/apache2/ /usr/sbin/entrypoint.sh && \
    chmod -R g=u /srv/www/htdocs/ /etc/apache2/ /usr/sbin/*apache* /var/log/apache2/ /usr/sbin/entrypoint.sh
ENTRYPOINT ["/usr/bin/catatonit", "--"]
CMD /usr/sbin/entrypoint.sh
EXPOSE 4200
USER $USER_ID
# Set base dir to the backend
ARG COMMIT
ARG BUILD_DATE
ARG APP_VERSION
ARG RSTATE
ARG TITLE
ARG IMAGE_PRODUCT_NUMBER
LABEL \
     org.opencontainers.image.title=$TITLE \
     org.opencontainers.image.created=$BUILD_DATE \
     org.opencontainers.image.revision=$COMMIT \
     org.opencontainers.image.vendor=Ericsson \
     org.opencontainers.image.version=$APP_VERSION \
     com.ericsson.product-revision="${RSTATE}" \
     com.ericsson.product-number="$IMAGE_PRODUCT_NUMBER"
