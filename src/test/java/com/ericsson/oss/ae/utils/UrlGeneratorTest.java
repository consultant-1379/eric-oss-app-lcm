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

package com.ericsson.oss.ae.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { UrlGenerator.class })
class UrlGeneratorTest {

    @Autowired
    UrlGenerator objectUnderTest;

    @Test
    void givenValidUrlGeneratorObject_WhenCallGetAppOnboardingHostAndPort_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.getAppOnboardingHostAndPort()).matches("http://eric-oss-app-onboarding:8080");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGetHelmOrchestratorHostAndPort_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.getHelmOrchestratorHostAndPort()).matches("http://eric-lcm-helm-executor:8888");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGetAppLcmHostAndPort_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.getAppLcmHostAndPort()).matches("http://eric-oss-app-lcm:8080");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateAppsUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateAppsUrl()).matches("http://eric-oss-app-onboarding:8080/v1/apps");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateAppByIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateAppByIdUrl(1L)).matches("http://eric-oss-app-onboarding:8080/v1/apps/1");

    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateArtifactsByAppIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateArtifactsByAppIdUrl(1L)).matches("http://eric-oss-app-onboarding:8080/v1/apps/1/artifacts");

    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateArtifactByAppIdAndArtifactIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateArtifactByAppIdAndArtifactIdUrl(1L, 1L))
                .matches("http://eric-oss-app-onboarding:8080/v1/apps/1/artifacts/1");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateAppArtifactFileUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateAppArtifactFileUrl(1L, 1L)).matches("http://eric-oss-app-onboarding:8080/v1/apps/1/artifacts/1/file");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateWorkloadInstanceUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateWorkloadInstancesUrl()).matches("http://eric-lcm-helm-executor:8888/cnwlcm/v1/workload_instances");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateWorkloadInstanceByIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateWorkloadInstanceByIdUrl("1111-1111-1111-1111"))
                .matches("http://eric-lcm-helm-executor:8888/cnwlcm/v1/workload_instances/1111-1111-1111-1111");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateWorkloadInstanceOperationsByIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateWorkloadInstanceOperationsByIdUrl("1111-1111-1111-1111"))
                .matches("http://eric-lcm-helm-executor:8888/cnwlcm/v1/workload_instances/1111-1111-1111-1111/operations");

    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateOperationsInstanceUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateOperationsInstanceUrl()).matches("http://eric-lcm-helm-executor:8888/cnwlcm/v1/operations");

    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateOperationsByIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateOperationsByIdUrl("0000-0000-0000-0001"))
                .matches("http://eric-lcm-helm-executor:8888/cnwlcm/v1/operations/0000-0000-0000-0001");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateOperationsLogsByIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateOperationsLogsByIdUrl("0000-0000-0000-0001"))
                .matches("http://eric-lcm-helm-executor:8888/cnwlcm/v1/operations/0000-0000-0000-0001/logs");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateAppInstancesUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateAppInstancesUrl()).matches("http://eric-oss-app-lcm:8080/app-lcm/v1/app-instances");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateAppInstanceByIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateAppInstanceByIdUrl(1L)).matches("http://eric-oss-app-lcm:8080/app-lcm/v1/app-instances/1");

    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateArtifactInstancesByAppIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateArtifactInstancesByAppIdUrl(1L))
                .matches("http://eric-oss-app-lcm:8080/app-lcm/v1/app-instances/1/artifact-instances");

    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateArtifactInstanceByAppIdAndArtifactIdUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateArtifactInstanceByAppIdAndArtifactIdUrl(1L, 1L))
                .matches("http://eric-oss-app-lcm:8080/app-lcm/v1/app-instances/1/artifact-instances/1");
    }


    @Test
    void givenValidUrlGeneratorObject_WhenCallGetKeycloakHostAndPort_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.getKeycloakHostAndPort()).matches("http://eric-sec-access-mgmt-http:8080");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateBearerTokenKeycloakUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateBearerTokenKeycloakUrl())
                .matches("http://eric-sec-access-mgmt-http:8080/auth/realms/master/protocol/openid-connect/token");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateClientKeycloakUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateClientKeycloakUrl("master"))
                .matches("http://eric-sec-access-mgmt-http:8080/auth/admin/realms/master/clients");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateSecretKeycloakUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateSecretKeycloakUrl("master", "1"))
                .matches("http://eric-sec-access-mgmt-http:8080/auth/admin/realms/master/clients/1/client-secret");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateClientScopeKeycloakUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateClientScopeKeycloakUrl("master"))
            .matches("http://eric-sec-access-mgmt-http:8080/auth/admin/realms/master/client-scopes");
    }

    @Test
    void givenValidUrlGeneratorObject_WhenCallGenerateClientWithAttributeKeycloakUrl_ThenReturnedUrlMatchesExpectedOutcome() {
        assertThat(objectUnderTest.generateClientWithAttributeKeycloakUrl("master"))
            .matches("http://eric-sec-access-mgmt-http:8080/auth/admin/realms/master/clients");
    }

}