DROP TABLE IF EXISTS credential_event;

CREATE TABLE credential_event
(
    id                          BIGSERIAL PRIMARY KEY,
    client_id                   VARCHAR(4000),
    app_on_boarding_app_id      BIGINT,
    app_instance_id             BIGINT,
    client_secret               VARCHAR(4000),
    health_status               VARCHAR(255),
    created_timestamp           TIMESTAMP NOT NULL,
    updated_timestamp           TIMESTAMP,
    client_scope                VARCHAR(255),
    deletion_status             VARCHAR(255)
    );

INSERT INTO credential_event(id, client_id, app_on_boarding_app_id, app_instance_id, client_secret, health_status, created_timestamp, updated_timestamp, client_scope, deletion_status)
VALUES(1, 'rApp_73c897540dfd', 1, 1, 'rApp_secret_7540dfd', 'INSTANTIATED', '2019-06-12', null,  'scope_test', 'PENDING_DELETION');
INSERT INTO credential_event(id, client_id, app_on_boarding_app_id, app_instance_id, client_secret, health_status, created_timestamp, updated_timestamp, client_scope, deletion_status)
VALUES(2, 'rApp_73c897540dfd', 1, 2, 'rApp_secret_7540dfd', 'INSTANTIATED', '2019-06-13', null,  'scope_test', 'PENDING_DELETION');
INSERT INTO credential_event(id, client_id, app_on_boarding_app_id, app_instance_id, client_secret, health_status, created_timestamp, updated_timestamp, client_scope, deletion_status)
VALUES(3, 'rApp_73c897540999', 1, 3, 'rApp_73c897540999', 'INSTANTIATED', '2019-06-12', null,  'scope_test_delete', 'PENDING_DELETION');

