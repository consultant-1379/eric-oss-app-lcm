/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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

package com.ericsson.oss.ae.clients.apponboarding;

import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.clients.apponboarding.dto.ArtifactDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interface specifying App Onboarding client calls.
 * <p>
 * Implementation of this interface {@link AppOnboardingClientImpl}.
 */
public interface AppOnboardingClient {
    ResponseEntity<AppDto> getAppById(Long appId);

    ResponseEntity<List<ArtifactDto>> getAppArtifacts(Long appId);

    ResponseEntity<ByteArrayResource> getAppArtifactFile(Long appId, Long artifactId);

    ResponseEntity<Void> updateStatusForDeletion(Long appId);

    ResponseEntity<Object> deletePackage(Long appId);
}
