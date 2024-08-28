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
ARG NODEJS_BUILDER_VERSION=16.16.0-1
ARG NODEJS_BUILDER_URL=armdocker.rnd.ericsson.se/proj-adp-cicd-drop/adp-nodejs-lts-builder-image

FROM $NODEJS_BUILDER_URL:$NODEJS_BUILDER_VERSION as nodejs

WORKDIR /eui_client

# Create non-privileged user. DR-D1123-122 is used to generate UID
ARG USER_ID=1000
RUN echo "$USER_ID:!::0:::::" >>/etc/shadow
ARG USER_NAME="eric-oss-app-lcm"
RUN echo "$USER_ID:x:$USER_ID:0:An Identity for $USER_NAME:/nonexistent:/bin/false" >>/etc/passwd

# Disable root login
RUN usermod -s /bin/false root && usermod -L root

RUN chown $USER_ID:0 -R  /opt/nodejs

COPY /eric-oss-app-ui/src ./src
COPY /eric-oss-app-ui/public ./public
COPY /eric-oss-app-ui/package.json \
/eric-oss-app-ui/server.js \
/eric-oss-app-ui/snowpack.config.js \
/eric-oss-app-ui/.npmrc \
/eric-oss-app-ui/LICENSE ./


ENV PATH=${PATH}:/opt/nodejs/latest/bin

# Install NPM dependencies
RUN npm run install:all
RUN npm run build:dev

USER $USER_ID

CMD npm run --prefix /eui_client  start
