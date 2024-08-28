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
    output_log=$($@ 2>&1 )
    log_message+="{\"timestamp\":\"$timestamp\","
    log_message+="\"version\":\"1.0.0\","
    log_message+="\"message\":\"$output_log\","
    log_message+="\"logger\":\"bash_logger\","
    log_message+="\"thread\":\"init app lcm db command: $@\","
    log_message+="\"path\":\"/scripts/init-db.sh\","
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
    #===DB-SQL-SCRIPT======
    cat << EOF | psql -d $APPMGRPGDATABASE
-- Create the database and customuser
CREATE DATABASE $PGDATABASE;
DO \$\$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = '$APP_LCM_DB_USER') THEN

      CREATE ROLE "$APP_LCM_DB_USER" LOGIN;
   END IF;
END
\$\$;
EOF
    cat << EOF | psql
-- Set timezone to UTC
SET TIMEZONE = 'UTC';
-- Create private schema for service
CREATE SCHEMA IF NOT EXISTS app_lcm_schema;
CREATE TABLE IF NOT EXISTS app_lcm_schema.app_instance
(
    id                     BIGSERIAL PRIMARY KEY,
    app_on_boarding_app_id BIGINT,
    health_status          VARCHAR(255) NOT NULL,
    target_status          VARCHAR(255) NOT NULL,
    created_timestamp      TIMESTAMPTZ NOT NULL,
    updated_timestamp      TIMESTAMPTZ,
    additional_parameters  VARCHAR(4000)
);
CREATE TABLE IF NOT EXISTS app_lcm_schema.artifact_instance
(
    id                          BIGSERIAL PRIMARY KEY,
    app_instance_id             BIGINT,
    app_on_boarding_artifact_id BIGINT,
    status_message              VARCHAR(4000),
    workload_instance_id        VARCHAR(255),
    operation_id                VARCHAR(255),
    health_status               VARCHAR(255),
    created_timestamp           TIMESTAMPTZ NOT NULL,
    updated_timestamp           TIMESTAMPTZ,
    CONSTRAINT fk_app_instance FOREIGN KEY (app_instance_id) references app_lcm_schema.app_instance (id)
);
CREATE TABLE IF NOT EXISTS app_lcm_schema.credential_event
(
    id                          BIGSERIAL PRIMARY KEY,
    client_id                   VARCHAR(4000),
    app_on_boarding_app_id      BIGINT,
    client_secret               VARCHAR(4000),
    health_status               VARCHAR(255),
    created_timestamp           TIMESTAMPTZ NOT NULL,
    updated_timestamp           TIMESTAMPTZ
);
-- Access control for service
GRANT ALL PRIVILEGES ON SCHEMA public TO $APP_LCM_DB_USER WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $APP_LCM_DB_USER WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON SCHEMA app_lcm_schema TO $APP_LCM_DB_USER WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA app_lcm_schema TO $APP_LCM_DB_USER WITH GRANT OPTION;
ALTER ROLE $APP_LCM_DB_USER IN DATABASE $PGDATABASE SET search_path to app_lcm_schema,public;
ALTER ROLE $APP_LCM_DB_USER WITH CREATEROLE;
REVOKE ALL ON ALL TABLES IN SCHEMA pg_catalog from public;
CREATE ROLE service_user;
GRANT SELECT ON ALL TABLES IN SCHEMA pg_catalog TO service_user;
GRANT service_user TO $APP_LCM_DB_USER;
ALTER TABLE app_lcm_schema.app_instance OWNER TO $APP_LCM_DB_USER;
ALTER TABLE app_lcm_schema.artifact_instance OWNER TO $APP_LCM_DB_USER;
ALTER TABLE app_lcm_schema.credential_event OWNER TO $APP_LCM_DB_USER;
ALTER TABLE app_lcm_schema.credential_event ADD COLUMN client_scope VARCHAR(255);
ALTER TABLE app_lcm_schema.credential_event ADD COLUMN deletion_status VARCHAR(255);
ALTER TABLE app_lcm_schema.credential_event ADD COLUMN app_instance_id BIGINT;
EOF
    #===
   if [ $? -eq 0 ];
     then
       command_wrapper echo "App-lcm db data initialized";
     else
       command_wrapper echo "Error initializing app-lcm db data";
       exit 1;
   fi
}

init_sql
