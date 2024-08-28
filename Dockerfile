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

# -- BASE OS Specific params --
ARG CBOS_IMAGE_TAG
ARG COMMON_BASE_OS_DOCKER_URL1=armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles
FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${CBOS_IMAGE_TAG} AS app-lcm

ARG CBOS_IMAGE_TAG
ARG CBO_REPO_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_IMAGE_TAG}

RUN zypper ar -C -G -f $CBO_REPO_URL?ssl_verify=no \
    COMMON_BASE_OS_SLES_REPO \
    && zypper install -l -y java-17-openjdk-headless \
    && zypper clean --all \
    && zypper rr COMMON_BASE_OS_SLES_REPO

ARG USER_ID=40514
RUN echo "$USER_ID:!::0:::::" >>/etc/shadow
ARG USER_NAME="eric-oss-app-lcm"
RUN echo "$USER_ID:x:$USER_ID:0:An Identity for $USER_NAME:/nonexistent:/bin/false" >>/etc/passwd

ARG JAR_VERSION=1.0.*-SNAPSHOT
ARG JAR_FILE=eric-oss-app-lcm-${JAR_VERSION}.jar
ADD target/${JAR_FILE} eric-oss-app-lcm.jar
COPY src/main/resources/jmx/* /jmx/
RUN chmod 600 /jmx/jmxremote.password
RUN chown $USER_ID /jmx/jmxremote.password

USER $USER_ID

CMD java ${JAVA_OPTS} --add-opens java.base/java.lang=ALL-UNNAMED \
-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=1099 \
-Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.rmi.port=1099 -Dcom.sun.management.jmxremote.password.file=/jmx/jmxremote.password \
-Dcom.sun.management.jmxremote.access.file=/jmx/jmxremote.access \
-Dsun.net.client.defaultConnectTimeout=40000 \
-Dsun.net.client.defaultReadTimeout=40000 \
-Djdk.tls.client.protocols=TLSv1.2 -jar eric-oss-app-lcm.jar

ARG COMMIT
ARG BUILD_DATE
ARG APP_VERSION
ARG RSTATE
ARG IMAGE_PRODUCT_NUMBER
LABEL \
    org.opencontainers.image.title=eric-oss-app-lcm-jsb \
    org.opencontainers.image.created=$BUILD_DATE \
    org.opencontainers.image.revision=$COMMIT \
    org.opencontainers.image.vendor=Ericsson \
    org.opencontainers.image.version=$APP_VERSION \
    com.ericsson.product-revision="${RSTATE}" \
    com.ericsson.product-number="$IMAGE_PRODUCT_NUMBER"
