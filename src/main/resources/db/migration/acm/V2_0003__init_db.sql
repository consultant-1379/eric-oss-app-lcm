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

DROP TABLE IF EXISTS acm_schema.app_event CASCADE;
DROP TABLE IF EXISTS acm_schema.permission CASCADE;
DROP TABLE IF EXISTS acm_schema.role CASCADE;
DROP TABLE IF EXISTS acm_schema.artifact CASCADE;
DROP TABLE IF EXISTS acm_schema.app_component CASCADE;

DROP TABLE IF EXISTS acm_schema.app_instance_event CASCADE;
DROP TABLE IF EXISTS acm_schema.client_credentials CASCADE;
DROP TABLE IF EXISTS acm_schema.app_instance CASCADE;

DROP TABLE IF EXISTS acm_schema.app;

CREATE TABLE IF NOT EXISTS acm_schema.app(
    id             UUID PRIMARY KEY,
    composition_id UUID,
    name           VARCHAR(255),
    version        VARCHAR(255),
    type           VARCHAR(50),
    mode           VARCHAR(25),
    status         VARCHAR(25),
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS acm_schema.app_component(
    id                       UUID PRIMARY KEY,
    app_id                   UUID,
    composition_element_name VARCHAR(255),
    name                     VARCHAR(255),
    version                  VARCHAR(255),
    type                     VARCHAR(50),
    CONSTRAINT fk_app FOREIGN KEY (app_id) REFERENCES acm_schema.app(id)
);

CREATE TABLE IF NOT EXISTS acm_schema.artifact(
    id               UUID PRIMARY KEY,
    app_component_id UUID,
    location         VARCHAR(255),
    name             VARCHAR(255),
    version          VARCHAR(255),
    type             VARCHAR(50),
    CONSTRAINT fk_app_component FOREIGN KEY (app_component_id) REFERENCES acm_schema.app_component(id)
);

CREATE TABLE IF NOT EXISTS acm_schema.app_event(
    id         BIGSERIAL PRIMARY KEY,
    app_id     UUID,
    type       VARCHAR(50),
    title      VARCHAR(50),
    detail     VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_app FOREIGN KEY (app_id) REFERENCES acm_schema.app(id)
);

CREATE TABLE IF NOT EXISTS acm_schema.permission(
    id       BIGSERIAL PRIMARY KEY,
    app_id   UUID,
    resource VARCHAR(50),
    scope    VARCHAR(50),
    CONSTRAINT fk_app FOREIGN KEY (app_id) REFERENCES acm_schema.app(id)
);

CREATE TABLE IF NOT EXISTS acm_schema.role(
    id     BIGSERIAL PRIMARY KEY,
    app_id UUID,
    name   VARCHAR(255),
    CONSTRAINT fk_app FOREIGN KEY (app_id) REFERENCES acm_schema.app(id)
);

CREATE TABLE IF NOT EXISTS acm_schema.app_instance(
    id                           UUID PRIMARY KEY,
    app_id                       UUID,
    target_app_id                UUID,
    composition_instance_id      UUID,
    status                       VARCHAR(25) NOT NULL,
    user_defined_helm_parameters VARCHAR(4000),
    created_at                   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMP,
    CONSTRAINT fk_app_id FOREIGN KEY (app_id) REFERENCES acm_schema.app(id)
);

CREATE TABLE IF NOT EXISTS acm_schema.app_component_instance(
    id                              UUID PRIMARY KEY,
    app_instance_id                 UUID,
    app_id                          UUID,
    app_component_id                UUID,
    composition_element_instance_id UUID,
    CONSTRAINT fk_app_instance FOREIGN KEY (app_instance_id) REFERENCES acm_schema.app_instance(id),
    CONSTRAINT fk_app_id FOREIGN KEY (app_id) REFERENCES acm_schema.app(id),
    CONSTRAINT fk_app_component FOREIGN KEY (app_component_id) REFERENCES acm_schema.app_component(id)
);

CREATE TABLE IF NOT EXISTS acm_schema.app_instance_event(
    id              BIGSERIAL PRIMARY KEY,
    app_instance_id UUID,
    type            VARCHAR(50),
    title           VARCHAR(50),
    detail          VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_app_instance FOREIGN KEY (app_instance_id) REFERENCES acm_schema.app_instance(id)
);

CREATE TABLE IF NOT EXISTS acm_schema.client_credentials (
    id                BIGSERIAL PRIMARY KEY,
    app_instance_id   UUID,
    client_id         VARCHAR(4000),
    client_secret     VARCHAR(4000),
    client_scope      VARCHAR(255),
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_app_instance FOREIGN KEY (app_instance_id) REFERENCES acm_schema.app_instance(id)
);

COMMIT;