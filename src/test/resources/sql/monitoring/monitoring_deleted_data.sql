-- Create private schema for service
DROP TABLE IF EXISTS artifact_instance;
DROP TABLE IF EXISTS app_instance;

--
CREATE TABLE app_instance
(
    id                     INTEGER PRIMARY KEY,
    app_on_boarding_app_id INTEGER,
    health_status          VARCHAR NOT NULL,
    target_status          VARCHAR NOT NULL,
    created_timestamp      TIMESTAMP NOT NULL,
    updated_timestamp      TIMESTAMP,
    additional_parameters  VARCHAR
);

CREATE TABLE artifact_instance
(
    id                          INTEGER PRIMARY KEY,
    app_on_boarding_artifact_id INTEGER,
    app_instance_id             INTEGER,
    status_message              VARCHAR(512),
    workload_instance_id        VARCHAR,
    operation_id                VARCHAR,
    health_status               VARCHAR,
    created_timestamp           TIMESTAMP NOT NULL,
    updated_timestamp           TIMESTAMP,
    FOREIGN KEY (app_instance_id) references app_instance(id)
);

--No Artifacts In Instance
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
 target_status, created_timestamp, updated_timestamp, additional_parameters) VALUES(1, 1, 'PENDING', 'INSTANTIATED', '2019-06-12', null,  '{"namespace":"test"}');

-- ///////////////////////////////////////////////////////////////////////////////////////////////////
 --Instance with Instantiated statuses of artifacts
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
 target_status, created_timestamp, updated_timestamp, additional_parameters) VALUES(2, 2, 'PENDING', 'INSTANTIATED', '2019-06-12', null,  '{"namespace":"test"}');

-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (1, 2, 2, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'INSTANTIATED',
  '2019-06-12', null);
  -- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (2, 2, 2, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'INSTANTIATED',
   '2019-06-12', null);

-- ///////////////////////////////////////////////////////////////////////////////////////////////////
 --Instance with Instantiated statuses of artifacts
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
 target_status, created_timestamp, updated_timestamp, additional_parameters) VALUES(3, 3, 'PENDING', 'INSTANTIATED', '2019-06-12', null,  '{"namespace":"test"}');

-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (3, 3, 3, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'INSTANTIATED',
  '2019-06-12', null);
  -- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (4, 3, 3, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'FAILED',
   '2019-06-12', null);

-- ///////////////////////////////////////////////////////////////////////////////////////////////////
 --Instance with pending statuses of artifacts
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
 target_status, created_timestamp, updated_timestamp, additional_parameters) VALUES(4, 4, 'PENDING', 'INSTANTIATED', '2019-06-12', null,  '{"namespace":"test"}');

-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (5, 4, 4, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'PENDING',
  '2019-06-12', null);
  -- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (6, 4, 4, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'PENDING',
   '2019-06-12', null);


-- ///////////////////////////////////////////////////////////////////////////////////////////////////
 --Instance with terminated one and instantiated rest statuses of artifacts
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
 target_status, created_timestamp, updated_timestamp, additional_parameters) VALUES(5, 5, 'PENDING', 'INSTANTIATED', '2019-06-12', null,  '{"namespace":"test"}');

-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (7, 5, 5, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'PENDING',
  '2019-06-12', null);
  -- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (8, 5, 5, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'TERMINATED',
   '2019-06-12', null);

 -- ///////////////////////////////////////////////////////////////////////////////////////////////////
  --Instance with terminated artifacts terminate instance
 INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
  target_status, created_timestamp, updated_timestamp, additional_parameters) VALUES(6, 6, 'PENDING', 'INSTANTIATED', '2019-06-12', null,  '{"namespace":"test"}');

 -- Creating artifactInstance
 INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
 status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (9, 6, 6, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'TERMINATED',
   '2019-06-12', null);
   -- Creating artifactInstance
 INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
 status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (10, 6, 6, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'TERMINATED',
    '2019-06-12', null);


 -- ///////////////////////////////////////////////////////////////////////////////////////////////////
  --Instance with terminated artifacts terminate instance
 INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
  target_status, created_timestamp, updated_timestamp, additional_parameters) VALUES(7, 7, 'PENDING', 'INSTANTIATED', '2019-06-12', null,  '{"namespace":"test"}');

 -- Creating artifactInstance
 INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
 status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (11, 7, 7, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'INSTANTIATED',
   '2019-06-12', null);
   -- Creating artifactInstance
 INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
 status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (12, 7, 7, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a32464', 'TERMINATED',
    '2019-06-12', null);


 -- ///////////////////////////////////////////////////////////////////////////////////////////////////
  --Instance with failed artifact.
 INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
  target_status, created_timestamp, updated_timestamp, additional_parameters) VALUES(8, 8, 'PENDING', 'INSTANTIATED', '2019-06-12', null,  '{"namespace":"test"}');

 -- Creating artifactInstance
 INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
 status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp) VALUES (13, 8, 8, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'INSTANTIATED',
   '2019-06-12', null);

-- ///////////////////////////////////////////////////////////////////////////////////////////////////
--Instance with deleted one and instantiated rest statuses of artifacts
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
                         target_status, created_timestamp, updated_timestamp, additional_parameters)
VALUES (9, 9, 'PENDING', 'APP_DELETED', '2019-06-12', null,  '{"namespace":"test"}');

-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (14, 9, 9, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'PENDING',
        '2019-06-12', null);
-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (15, 9, 9, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'DELETED',
        '2019-06-12', null);

-- ///////////////////////////////////////////////////////////////////////////////////////////////////
--Instance with deleted artifacts delete instance
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
                         target_status, created_timestamp, updated_timestamp, additional_parameters)
VALUES (10, 10, 'PENDING', 'APP_DELETED', '2019-06-12', null,  '{"namespace":"test"}');

-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (16, 10, 10, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'DELETED',
        '2019-06-12', null);
-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (17, 10, 10, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'DELETED',
        '2019-06-12', null);

-- ///////////////////////////////////////////////////////////////////////////////////////////////////
--Instance for artefact get
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
                         target_status, created_timestamp, updated_timestamp, additional_parameters)
VALUES (11, 11, 'PENDING', 'INSTANTIATED', '2019-06-12', null,  '{"namespace":"test"}');

-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (18, 11, 11, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'PENDING',
        '2019-06-12', null);
-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (19, 11, 11, 'Error:helloworld app already deployed', '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'FAILED',
        '2019-06-12', null);
-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (20, 11, 11, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'INSTANTIATED',
        '2019-06-12', null);
-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (21, 11, 11, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'TERMINATED',
        '2019-06-12', null);
-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (22, 11, 11, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'DELETED',
        '2019-06-12', null);

-- ///////////////////////////////////////////////////////////////////////////////////////////////////
--Instance with target status DELETED and health status DELETING
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
                         target_status, created_timestamp, updated_timestamp, additional_parameters)
VALUES (12, 12, 'DELETING', 'APP_DELETED', '2019-06-12', null,  '{"namespace":"test"}');

-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (23, 12, 12, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'DELETED',
        '2019-06-12', null);
-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (24, 12, 12, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'DELETED',
        '2019-06-12', null);

-- ///////////////////////////////////////////////////////////////////////////////////////////////////
--Instance with target status DELETED and health status FAILED
INSERT INTO app_instance(id, app_on_boarding_app_id, health_status,
                         target_status, created_timestamp, updated_timestamp, additional_parameters)
VALUES (13, 13, 'FAILED', 'APP_DELETED', '2019-06-12', null,  '{"namespace":"test"}');

-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (25, 13, 13, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'DELETED',
        '2019-06-12', null);
-- Creating artifactInstance
INSERT INTO artifact_instance(id, app_on_boarding_artifact_id, app_instance_id,
                              status_message, workload_instance_id, operation_id, health_status, created_timestamp, updated_timestamp)
VALUES (26, 13, 13, null, '0383cea8-9ea4-42c7-b5b7-73c897540dfd', 'd9920094-8a60-413d-a975-c97d36a3246d', 'DELETED',
        '2019-06-12', null);