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

package com.ericsson.oss.ae.acm.presentation.mapper;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APPS;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_INSTANCES;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.APP_LCM_RESOURCE_PATH_V3;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.SLASH;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;

/**
 * URL Generator for LCM endpoints
 */
@Component
@Slf4j
public class LcmUrlGenerator {

    @Value("${APP_MANAGER_APP_LCM_ROUTE_PATH:/app-manager/lcm}")
    private String appManagerAppLcmRoutePath;

    /**
     * Composes URL for get apps by ID
     *
     * @param appId app id
     * @return Returns App LCM URL for getting apps by id
     */
    public String getAppsUrlById(final UUID appId) {
        final String baseUrlForApp = generateBaseLcmV3Url() + APPS + SLASH + appId;
        return generateUriPath(baseUrlForApp);
    }

    /**
     * Composes URL for get app instance by ID
     *
     * @param appInstanceId app instance id
     * @return Returns App LCM URL for getting app instance by id
     */
    public String getAppsInstanceUrlById(final String appInstanceId) {
        final String baseUrlForAppInstance = generateBaseLcmV3Url() + APP_INSTANCES + SLASH + appInstanceId;
        return generateUriPath(baseUrlForAppInstance);
    }

    private String generateUriPath(final String appLcmResourcePath) {
        log.info("generating URL for resource path: {}", appLcmResourcePath);
        try {
            return new URI(appLcmResourcePath).toString();
        } catch (final URISyntaxException ex) {
            log.error("Error generating URL for resource path: {} for reason: {}", appLcmResourcePath, ex.getMessage());
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.APP_LCM_URI_GENERATION_ERROR);
        }
    }

    private String generateBaseLcmV3Url() {
        return APP_LCM_RESOURCE_PATH_V3;
    }

}
