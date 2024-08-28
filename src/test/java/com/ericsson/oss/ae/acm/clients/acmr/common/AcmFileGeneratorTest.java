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

package com.ericsson.oss.ae.acm.clients.acmr.common;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.AC_INSTANCE_PROPERTIES_GENERATION_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.model.CompositionInstanceData;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponent;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

/**
 * Test class for the AcmFileGenerator.
 */
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AppLcmApplication.class, AcmFileGenerator.class})
public class AcmFileGeneratorTest {
    private static final String TOSCA_SERVICE_TEMPLATE_YAML = "src/test/resources/acmfiles/ToscaServiceTemplate.yaml";
    private static final String TOSCA_SERVICE_TEMPLATE_YAML_WITH_DATAMANAGEMENT_COMPONENT = "src/test/resources/acmfiles/ToscaServiceTemplateWithDataManagementComponent.yaml";
    private static final String COMPOSITION_INSTANCE_PROPERTIES_YAML = "src/test/resources/acmfiles/AcInstancePropertiesCreate.yaml";
    private static final String COMPOSITION_INSTANCE_PROPERTIES_YAML_WITH_COMPONENT_INSTANCES_PROPERTIES = "src/test/resources/acmfiles/AcInstancePropertiesUpdate.yaml";
    @Autowired
    private AcmFileGeneration acmFileGeneration;

    private App app;

    @BeforeEach
    public void setUp() {
        // Initialize the createAppRequest test fixture variable with a CreateAppRequest object.
        this.app = TestUtils.generateAppEntity();
    }

    @Test
    public void testGenerateAcmServiceTemplate_ValidCreateAppRequestObject() throws IOException {
        // Given
        final String expectedAcmServiceTemplate = new String(
                Files.readAllBytes(Paths.get(TOSCA_SERVICE_TEMPLATE_YAML)));

        // When
        final String generatedAcmServiceTemplate = this.acmFileGeneration
                .generateToscaServiceTemplate(this.app);

        // Then
        assertEquals(expectedAcmServiceTemplate, generatedAcmServiceTemplate);
    }

    @Test
    public void testGenerateAcmServiceTemplate_WithMicroserviceAndDataManagementComponents_ValidCreateAppRequestObject() throws IOException {
        // Given
        final String expectedAcmServiceTemplateMultipleComponents = new String(
                Files.readAllBytes(Paths.get(TOSCA_SERVICE_TEMPLATE_YAML_WITH_DATAMANAGEMENT_COMPONENT)));

        // When
        final String generatedAcmServiceTemplateMultipleComponents = this.acmFileGeneration
                .generateToscaServiceTemplate(TestUtils.generateAppEntityWithMicroserviceAndDataManagementComponents(AppMode.DISABLED, AppStatus.CREATED));

        // Then
        assertEquals(expectedAcmServiceTemplateMultipleComponents, generatedAcmServiceTemplateMultipleComponents);
    }

    @Test
    public void testGenerateAcmServiceTemplate_WithMicroserviceAndDataManagementComponents_InvalidCreateAppRequestObject() throws IOException {
        // Given
        final App appWithMicroserviceAndDataManagementComponents= TestUtils.generateAppEntityWithMicroserviceAndDataManagementComponents(AppMode.DISABLED, AppStatus.CREATED);
        final AppComponent dataManagementComponent = appWithMicroserviceAndDataManagementComponents.getAppComponents().get(1);
        dataManagementComponent.setCompositionElementName(null);

        // When
        final AppLcmException appLcmException = assertThrows(
                AppLcmException.class, () -> this.acmFileGeneration
                        .generateToscaServiceTemplate(appWithMicroserviceAndDataManagementComponents)
        );

        // Then
        assertEquals(
                INTERNAL_SERVER_ERROR,
                appLcmException.getHttpStatus()
        );

        assertEquals(
                TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR.getErrorTitle(),
                appLcmException.getAppLcmError().getErrorTitle()
        );

        assertEquals(
                TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR.getErrorMessage(),
                appLcmException.getAppLcmError().getErrorMessage()
        );
    }

    @Test
    public void testGenerateAcmServiceTemplate_InvalidCreateAppRequestObject() {
        // Given
        this.app.setAppComponents(null);

        // When
        final AppLcmException appLcmException = assertThrows(
                AppLcmException.class, () -> this.acmFileGeneration
                        .generateToscaServiceTemplate(this.app)
        );

        // Then
        assertEquals(
                INTERNAL_SERVER_ERROR,
                appLcmException.getHttpStatus()
        );

        assertEquals(
                TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR.getErrorTitle(),
                appLcmException.getAppLcmError().getErrorTitle()
        );

        assertEquals(
                TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR.getErrorMessage(),
                appLcmException.getAppLcmError().getErrorMessage()
        );
    }


    @Test
    public void testGenerateAcmInstance_WithMultipleComponents_ValidAcmInstanceObject() throws IOException {
        // Given
        final CompositionInstanceData compositionInstanceData = TestUtils.generateValidCompositionInstanceData();
        compositionInstanceData.getAppInstance().setCompositionInstanceId(null);
        final String expectedAcmInstance = new String(
                Files.readAllBytes(Paths.get(
                        COMPOSITION_INSTANCE_PROPERTIES_YAML
                )));

        // When
        final String generatedAcmInstance = this.acmFileGeneration.generateAcmInstancePropertiesFile(compositionInstanceData);

        // Then
        // Compare ignoring whitespace differences
        assertEquals(expectedAcmInstance.replaceAll("\\s+", ""), generatedAcmInstance.replaceAll("\\s+", ""));
    }

    @Test
    public void testGenerateAcmInstance_InvalidAcmInstanceObject() {
        // Given
        final CompositionInstanceData invalidCompositionInstanceData = TestUtils.generateInvalidCompositionInstanceData();

        // When
        final AppLcmException appLcmException = assertThrows(
                AppLcmException.class, () -> this.acmFileGeneration
                        .generateAcmInstancePropertiesFile(invalidCompositionInstanceData)
        );

        // Then
        assertEquals(
                INTERNAL_SERVER_ERROR,
                appLcmException.getHttpStatus()
        );

        assertEquals(
                AC_INSTANCE_PROPERTIES_GENERATION_ERROR.getErrorTitle(),
                appLcmException.getAppLcmError().getErrorTitle()
        );

        assertEquals(
                AC_INSTANCE_PROPERTIES_GENERATION_ERROR.getErrorMessage(),
                appLcmException.getAppLcmError().getErrorMessage()
        );

    }

    @Test
    public void testGenerateAcmInstance_ValidAcmInstanceObjectWithComponentInstanceProperties() throws IOException {
        // Given
        final CompositionInstanceData compositionInstanceData = TestUtils.generateValidCompositionInstanceDataWithComponentInstancesProperties();
        final String expectedAcmInstance = new String(
            Files.readAllBytes(Paths.get(
                COMPOSITION_INSTANCE_PROPERTIES_YAML_WITH_COMPONENT_INSTANCES_PROPERTIES
            )));

        // When
        final String generatedAcmInstance = this.acmFileGeneration.generateAcmInstancePropertiesFile(compositionInstanceData);

        // Then
        // Compare ignoring whitespace differences
        assertEquals(expectedAcmInstance.replaceAll("\\s+", ""), generatedAcmInstance.replaceAll("\\s+", ""));

    }

    @Test
    public void testGenerateAcmInstance_InvalidAcmInstanceObjectWithComponentInstanceProperties() throws IOException {
        // Given
        final CompositionInstanceData invalidCompositionInstanceData = TestUtils.generateInvalidCompositionInstanceDataWithComponentInstancesProperties();

        // When
        final AppLcmException appLcmException = assertThrows(
            AppLcmException.class, () -> this.acmFileGeneration
                .generateAcmInstancePropertiesFile(invalidCompositionInstanceData)
        );

        // Then
        assertEquals(
            INTERNAL_SERVER_ERROR,
            appLcmException.getHttpStatus()
        );

        assertEquals(
            AC_INSTANCE_PROPERTIES_GENERATION_ERROR.getErrorTitle(),
            appLcmException.getAppLcmError().getErrorTitle()
        );

        assertEquals(
            AC_INSTANCE_PROPERTIES_GENERATION_ERROR.getErrorMessage(),
            appLcmException.getAppLcmError().getErrorMessage()
        );
    }

}
