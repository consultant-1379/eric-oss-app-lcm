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
package com.ericsson.oss.ae.acm.clients.acmr.rest;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.ACM_BASE_URL;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.ACM_COMPOSITION;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.ACM_INSTANCE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.HTTP;
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

@Component
@Slf4j
public class AcmUrlGenerator {

    @Value("${ACM_SERVICE_HOSTNAME:eric-oss-acm-runtime}")
    private String acmHostname;

    @Value("${ACM_SERVICE_PORT:6969}")
    private String acmPort;

    public String getAcmCompositionUrl() {
        return generateUri(generateAcmCompositionUrl());
    }

    public String getAcmCompositionUrlWithCompositionId(final UUID compositionId) {
        return generateUri(generateAcmCompositionUrlWithId(compositionId));
    }

    public String generateAcmCompositionUrlWithInstance(final UUID compositionId) {
        final String compositionsResourcePath = generateAcmCompositionInstanceUrl(compositionId);
        return generateUri(compositionsResourcePath);
    }

    public String generateUrlForSpecificAcmCompositionInstance(final UUID compositionId, final UUID compositionInstanceId) {
        final String compositionInstancePath = generateAcmCompositionInstanceUrlWithId(compositionId, compositionInstanceId);
        return generateUri(compositionInstancePath);
    }

    private String generateUri(final String path) {
        log.debug("generating URI for hostName : {}", acmHostname);
        try {
            return new URI(HTTP, null, acmHostname, Integer.parseInt(acmPort), path, null, null).toString();
        } catch (final URISyntaxException ex) {
            log.error("Error generating URL for hostName: {} for reason: {}", acmHostname, ex.getMessage());
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.ACM_URL_GENERATION_ERROR);
        }
    }

    private String generateAcmCompositionUrl() {
        return ACM_BASE_URL + SLASH + ACM_COMPOSITION;
    }

    private String generateAcmCompositionUrlWithId(final UUID compositionId) {
        return  generateAcmCompositionUrl() + SLASH + compositionId;
    }

    private String generateAcmCompositionInstanceUrl(final UUID compositionId) {
        return  generateAcmCompositionUrlWithId(compositionId) + SLASH + ACM_INSTANCE;
    }

    private String generateAcmCompositionInstanceUrlWithId(final UUID compositionId, final UUID instanceId) {
        return  generateAcmCompositionInstanceUrl(compositionId) + SLASH + instanceId;
    }
}
