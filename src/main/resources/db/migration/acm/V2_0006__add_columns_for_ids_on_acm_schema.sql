--------------------------------------------------------------------------------------------------------------------------------------------------------------
-- COPYRIGHT Ericsson 2024
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

    ALTER TABLE IF EXISTS acm_schema.app
      ADD rapp_id varchar(255),
      ADD provider varchar(255);

    ALTER TABLE IF EXISTS acm_schema.app_instance
      ADD rapp_instance_id varchar(255);

COMMIT;