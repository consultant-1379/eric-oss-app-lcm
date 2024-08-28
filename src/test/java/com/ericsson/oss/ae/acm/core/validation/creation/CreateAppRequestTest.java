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

package com.ericsson.oss.ae.acm.core.validation.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.ae.v3.api.model.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.acm.common.constant.AppLcmConstants;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.v3.api.model.Artifact;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { CreateAppRequestValidator.class})
public class CreateAppRequestTest {

    private CreateAppRequest createAppRequest;

    @BeforeEach
    private void setup() {
         createAppRequest = setupCreateAppRequest();
    }

    @Test
    public void testMissingAppName() {
        createAppRequest.setName("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_NAME_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.setName(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_NAME_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testMissingAppVersion() {
        createAppRequest.setVersion("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_VERSION_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.setVersion(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_VERSION_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testInvalidAppVersionFormat() {
        createAppRequest.setVersion("123");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_INVALID_APP_VERSION_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.setVersion("a.b.c");
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_INVALID_APP_VERSION_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testMissingAppType() {
        createAppRequest.setType("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_TYPE_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.setType(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_TYPE_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testMissingAppProvider() {
        createAppRequest.setProvider("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_PROVIDER_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.setProvider(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_PROVIDER_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testMissingAppComponentName() {
        createAppRequest.getComponents().get(0).setName("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_COMPONENT_NAME_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.getComponents().get(0).setName(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_COMPONENT_NAME_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testMissingAppComponentType() {
        createAppRequest.getComponents().get(0).setType("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_COMPONENT_TYPE_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.getComponents().get(0).setType(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_COMPONENT_TYPE_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testInvalidAppComponentType() {
        createAppRequest.getComponents().get(0).setType("Test");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_APP_COMPONENT_TYPE_UNSUPPORTED_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testMissingAppComponentVersion() {
        createAppRequest.getComponents().get(0).setVersion("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_COMPONENT_VERSION_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.getComponents().get(0).setVersion(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_MISSING_APP_COMPONENT_VERSION_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testInvalidAppComponentVersionFormat() {
        createAppRequest.getComponents().get(0).setVersion("123");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_INVALID_APP_COMPONENT_VERSION_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.getComponents().get(0).setVersion("a.b.c");
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_INVALID_APP_COMPONENT_VERSION_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testMissingArtifactName() {
        createAppRequest.getComponents().get(0).getArtifacts().get(0).setName("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_MISSING_NAME_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.getComponents().get(0).getArtifacts().get(0).setName(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_MISSING_NAME_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testMissingArtifactType() {
        createAppRequest.getComponents().get(0).getArtifacts().get(0).setType("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_MISSING_TYPE_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.getComponents().get(0).getArtifacts().get(0).setType(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_MISSING_TYPE_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testMissingArtifactLocation() {
        createAppRequest.getComponents().get(0).getArtifacts().get(0).setLocation("");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_MISSING_LOCATION_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.getComponents().get(0).getArtifacts().get(0).setLocation(null);
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_MISSING_LOCATION_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testInvalidArtifactLocation() {
        createAppRequest.getComponents().get(0).getArtifacts().get(0).setLocation("testBucket/");
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_LOCATION_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.getComponents().get(0).getArtifacts().get(0).setLocation("/testLocation");
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_LOCATION_ERR_MESSAGE), exception.generateErrorMessage());
        createAppRequest.getComponents().get(0).getArtifacts().get(0).setLocation("testLocation");
        exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_ARTIFACT_LOCATION_ERR_MESSAGE), exception.generateErrorMessage());
    }

    @Test
    public void testNullAppComponent() {
        List<Component> emptyComponentList = new ArrayList<>();
        createAppRequest.setComponents(emptyComponentList);
        AppLcmException exception = assertThrows(AppLcmException.class, () -> new CreateAppRequestValidator().validate(createAppRequest));
        assertEquals(String.format(AppLcmError.CREATE_APP_REQUEST_VALIDATION_FAILED.getErrorMessage(), AppLcmConstants.CREATE_APP_REQUEST_NULL_APP_COMPONENT_ERR_MESSAGE), exception.generateErrorMessage());
    }

    private CreateAppRequest setupCreateAppRequest() {
        CreateAppRequest createAppRequest = new CreateAppRequest();
        createAppRequest.setName("TestApp");
        createAppRequest.setType("Microservice");
        createAppRequest.setProvider("Ericsson");
        createAppRequest.setVersion("3.10.0-Alpha");
        Component appComponent = new Component();
        appComponent.setName("TestApp");
        appComponent.setType("Microservice");
        appComponent.setVersion("1.1.1");
        Artifact artifact = new Artifact();
        artifact.setName("TestArtifact");
        artifact.setType("artifactTypeTest");
        artifact.setLocation("testBucket/testLocation");
        appComponent.addArtifactsItem(artifact);
        createAppRequest.addComponentsItem(appComponent);
        return createAppRequest;
    }
}
