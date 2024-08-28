/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.ae.acm.clients.minio;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;

import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;

/**
 * Service that integrates with the object-store-mn microservice
 *
 */
@Service
public class ObjectStoreService {

    private static final Logger logger = LoggerFactory.getLogger(ObjectStoreService.class);

    @Autowired
    private MinioClient minioClient;

    /**
     * Delete all objects recursively from a bucket in the object store using the provided bucket name.
     *
     * @param bucketName The name of the bucket
     * @param jobId The unique identifier (UUID) of the onboarding-job
     */
    public void deleteAllObjectsInBucket(final String bucketName, final UUID jobId) {
        try {
            logger.debug("deleteAllObjectsInBucket() Deleting all objects recursively from the bucket: {}", bucketName);
            final boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (found) {
                // List all objects in the bucket recursively
                final Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(jobId.toString()).recursive(true).build());

                for (final Result<Item> item : items) {
                    final String objectName = item.get().objectName();
                    // Delete the object
                    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
                    logger.info("deleteAllObjectsInBucket() object: {} deleted from the bucket: {}.", objectName, bucketName);
                }
            } else {
                logger.info("deleteAllObjectsInBucket() bucket: {} does not exist. No further action needed.", bucketName);
            }
        } catch (final Exception e) {
            logger.error("deleteAllObjectsInBucket() Error while deleting objects from the bucket: {}", bucketName, e);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.SERVER_ERROR_OBJECT_STORE_ARTIFACT_DELETE);
        }
    }
}
