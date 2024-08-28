--------------------------------------------------------------------------------------------------------------------------------------------------------------
-- COPYRIGHT Ericsson 2023
--
--
--
-- The copyright to the computer program(s) herein is the property of
--
-- Ericsson Inc. The programs may be used and/or copied only with written
--
-- permission from Ericsson Inc. or in accordance with the terms and
--
-- conditions stipulated in the agreement/contract under which the
--
-- program(s) have been supplied.
------------------------------------------------------------------------------------------------------------------------------------------------------------/
BEGIN TRANSACTION;

    CREATE TABLE IF NOT EXISTS acm_schema.app(
        id UUID PRIMARY KEY,
        acm_composition_id VARCHAR(36),
        name VARCHAR(255),
        version VARCHAR(255),
        mode VARCHAR(255),
        status VARCHAR(255),
        type VARCHAR(255),
        created_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
        updated_timestamp TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS acm_schema.app_event(
        id BIGSERIAL PRIMARY KEY,
        type VARCHAR(50),
        title VARCHAR(255),
        detail VARCHAR(255),
        app_id UUID,
        timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
        CONSTRAINT fk_app FOREIGN KEY (app_id) REFERENCES acm_schema.app(id)
    );

    CREATE TABLE IF NOT EXISTS acm_schema.app_component(
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(255),
        version VARCHAR(255),
        type VARCHAR(10),
        app_id UUID,
        CONSTRAINT fk_app FOREIGN KEY (app_id) REFERENCES acm_schema.app(id)
    );

    CREATE TABLE IF NOT EXISTS acm_schema.artifact(
        id BIGSERIAL PRIMARY KEY,
        location VARCHAR(255),
        name VARCHAR(255),
        type VARCHAR(10),
        version VARCHAR(255),
        app_component_id BIGINT,
        CONSTRAINT fk_app_component FOREIGN KEY (app_component_id) REFERENCES acm_schema.app_component(id)
    );

    CREATE TABLE IF NOT EXISTS acm_schema.permission(
        id BIGSERIAL PRIMARY KEY,
        resource VARCHAR(255),
        scope VARCHAR(255),
        app_id UUID,
        CONSTRAINT fk_app FOREIGN KEY (app_id) REFERENCES acm_schema.app(id)
    );

    CREATE TABLE IF NOT EXISTS acm_schema.role(
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(255),
        app_id UUID,
        CONSTRAINT fk_app FOREIGN KEY (app_id) REFERENCES acm_schema.app(id)
    );

    CREATE TABLE IF NOT EXISTS acm_schema.app_instance(
        id UUID PRIMARY KEY,
        app_id UUID,
        acm_instance_id VARCHAR(36),
        status VARCHAR(255) NOT NULL,
        created_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
        updated_timestamp TIMESTAMP,
        additional_parameters VARCHAR(4000),
        CONSTRAINT fk_app_id FOREIGN KEY (app_id) references acm_schema.app (id)
    );

    CREATE TABLE IF NOT EXISTS acm_schema.app_instance_event(
        id BIGSERIAL PRIMARY KEY,
        type VARCHAR(50),
        title VARCHAR(255),
        detail VARCHAR(255),
        app_instance_id UUID,
        timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
        CONSTRAINT fk_app_instance FOREIGN KEY (app_instance_id) REFERENCES acm_schema.app_instance(id)
    );

    CREATE TABLE IF NOT EXISTS acm_schema.client_credentials (
        id BIGSERIAL PRIMARY KEY,
        client_id VARCHAR(4000),
        app_instance_id UUID,
        client_secret VARCHAR(4000),
        status VARCHAR(255),
        client_scope VARCHAR(255),
        created_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
        updated_timestamp TIMESTAMP,
        CONSTRAINT fk_app_instance FOREIGN KEY (app_instance_id) REFERENCES acm_schema.app_instance(id)
    );

COMMIT;