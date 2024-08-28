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

package com.ericsson.oss.ae.acm.clients.minio.config;

import static com.ericsson.oss.ae.acm.TestConstants.OBJECT_STORE_ACCESS_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.LOCAL_HOSTNAME;
import static com.ericsson.oss.ae.acm.TestConstants.OBJECT_STORE_SECRET_KEY;
import static com.ericsson.oss.ae.constants.AppLcmConstants.HTTPS;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.minio.ObjectStoreContainer;

import io.minio.MinioClient;

@TestConfiguration
public class MinioTestClient {

    private static ObjectStoreContainer objectStoreContainer = null;

    /**
     * Init minio client.
     *
     * @return the minio client
     */
    @Bean(name = "minioClient")
    @Primary
    @Lazy
    public MinioClient initMinioClient() {
        if (objectStoreContainer == null) {
            objectStoreContainer = TestUtils.initObjectStoreContainer();
        }
        final MinioClient.Builder minioBuilder = MinioClient.builder();
        final int mappedPort = objectStoreContainer.getPort();
        return minioBuilder.endpoint(HTTPS + LOCAL_HOSTNAME, mappedPort, false)
            .credentials(OBJECT_STORE_ACCESS_KEY, OBJECT_STORE_SECRET_KEY).build();
    }
}