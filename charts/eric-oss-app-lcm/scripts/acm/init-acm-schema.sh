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

function command_wrapper {
    log_message=''
    timestamp=`date +%G-%m-%eT%T.%3N`
    output_log=$("$@" 2>&1 )
    log_message+="{\"timestamp\":\"$timestamp\","
    log_message+="\"version\":\"1.0.0\","
    log_message+="\"message\":\"$output_log\","
    log_message+="\"logger\":\"bash_logger\","
    log_message+="\"thread\":\"init acm schema command: $@\","
    log_message+="\"path\":\"/scripts/acm/init-acm-schema.sh\","
    log_message+="\"service_id\":\"eric-oss-app-lcm\","
    log_message+="\"severity\":\"info\"}"
    echo $log_message
}

function init_sql {
    until
     pg_isready; do
       command_wrapper echo "Database instance $PGHOST is not ready. Waiting ..."
       sleep 3
    done
  cat << EOF | psql
-- Set up ACM Schema and Permissions
CREATE SCHEMA IF NOT EXISTS acm_schema;
GRANT ALL PRIVILEGES ON SCHEMA acm_schema TO $APP_LCM_DB_USER WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA acm_schema TO $APP_LCM_DB_USER WITH GRANT OPTION;

-- Configure Search Path and Role Permissions
ALTER ROLE $APP_LCM_DB_USER IN DATABASE $PGDATABASE SET search_path to app_lcm_schema,public,acm_schema;
ALTER ROLE $APP_LCM_DB_USER WITH CREATEROLE;

-- Assign Ownership of Specific Tables
ALTER TABLE IF EXISTS acm_schema.app OWNER TO $APP_LCM_DB_USER;
ALTER TABLE IF EXISTS acm_schema.app_event OWNER TO $APP_LCM_DB_USER;
ALTER TABLE IF EXISTS acm_schema.app_component OWNER TO $APP_LCM_DB_USER;
ALTER TABLE IF EXISTS acm_schema.artifact OWNER TO $APP_LCM_DB_USER;
ALTER TABLE IF EXISTS acm_schema.permission OWNER TO $APP_LCM_DB_USER;
ALTER TABLE IF EXISTS acm_schema.role OWNER TO $APP_LCM_DB_USER;
ALTER TABLE IF EXISTS acm_schema.app_instance OWNER TO $APP_LCM_DB_USER;
ALTER TABLE IF EXISTS acm_schema.client_credentials OWNER TO $APP_LCM_DB_USER;
ALTER TABLE IF EXISTS acm_schema.app_instance_event OWNER TO $APP_LCM_DB_USER;
EOF
    #==== Operation Complete =====
   if [ $? -eq 0 ];
     then
       command_wrapper echo "App-lcm db data initialized";
     else
       command_wrapper echo "Error initializing app-lcm db data";
       exit 1;
   fi
}

init_sql