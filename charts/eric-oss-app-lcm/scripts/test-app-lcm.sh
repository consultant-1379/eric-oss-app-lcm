#
# COPYRIGHT Ericsson 2023
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

#! /bin/bash

sleep 240

status=$(curl --silent --output /dev/null -w '%{http_code}' --connect-timeout 20 http://$SERVICE_HOSTNAME:$SERVICE_PORT/actuator/health) && if [[ $status -eq 200 ]]; then exit 0; else exit 1; fi
