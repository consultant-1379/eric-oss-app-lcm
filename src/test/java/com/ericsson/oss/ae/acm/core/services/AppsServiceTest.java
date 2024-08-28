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

package com.ericsson.oss.ae.acm.core.services;

import static com.ericsson.oss.ae.acm.TestConstants.AUTOMATION_COMPOSITION_DEFINITION_JSON;
import static com.ericsson.oss.ae.acm.TestConstants.TEST_STRING;
import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.rest.AcmUrlGenerator;
import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

@SpringBootTest(classes = {AppLcmApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:acm/application-test.properties")
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppsServiceTest {

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppInstancesRepository appInstancesRepository;

    @Autowired
    private AppServiceImpl appsService;

    @Autowired
    private AcmUrlGenerator acmUrlGenerator;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private MinioClient minioClient;
    private MockRestServiceServer mockServer;

    protected MockitoSession session;

    @BeforeAll
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    public void tearDown() {
        appRepository.deleteAll();

        if (session != null) {
            session.finishMocking();
        }

        mockServer.reset();
    }

    @Test
    public void testDeleteAppById_failed_appInstance_exist() {
        // Arrange
        final AppInstances appInstance = TestUtils.generateAppInstanceEntity();
        appInstance.getApp().setMode(AppMode.DISABLED);
        final App app = appRepository.save(appInstance.getApp());
        appInstance.setApp(app);
        appInstancesRepository.save(appInstance);

        // Act
        final AppLcmException exception = Assertions.assertThrows(AppLcmException.class, () -> {
            appsService.deleteAppById(app.getId().toString());
        });

        // Assert
        Assertions.assertTrue(
                exception.getAppLcmError().getErrorMessage().contains(AppLcmError.BAD_REQUEST_ERROR_DELETE_APP_INSTANCES_EXIST.getErrorMessage()));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    public void testDeleteAppById_failed_app_notExist() {
        // Arrange & Act
        final AppLcmException exception = Assertions.assertThrows(AppLcmException.class, () -> {
            appsService.deleteAppById(UUID.randomUUID().toString());
        });

        // Assert
        Assertions.assertTrue(
                exception.getAppLcmError().getErrorMessage().contains(AppLcmError.APP_NOT_FOUND_ERROR.getErrorMessage()));
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    public void testDeleteAppById_failed_app_mode_enabled() {
        // Arrange
        final App app = appRepository.save(TestUtils.generateAppEntity(AppMode.ENABLED, AppStatus.CREATED));

        // Act
        final AppLcmException exception = Assertions.assertThrows(AppLcmException.class, () -> {
            appsService.deleteAppById(app.getId().toString());
        });

        // Assert
        Assertions.assertTrue(
                exception.getAppLcmError().getErrorMessage().contains(AppLcmError.LCM_APP_MODE_VALIDATION_ERROR.getErrorMessage()));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    public void testDeleteAppById_failed_app_status_not_valid() {
        // Arrange
        final App app = appRepository.save(TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.INITIALIZED));

        // Act
        final AppLcmException exception = Assertions.assertThrows(AppLcmException.class, () -> {
            appsService.deleteAppById(app.getId().toString());
        });

        // Assert
        Assertions.assertTrue(
                exception.getAppLcmError().getErrorMessage().contains(AppLcmError.LCM_STATUS_VALIDATION_ERROR.getErrorMessage()));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    public void testDeleteAppById_success_acmCompositionDef_not_found() throws Exception {
        // Arrange
        final App appUnderTest = appRepository.save(TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED));
        final String url = acmUrlGenerator.getAcmCompositionUrl() + "/" + appUnderTest.getCompositionId();
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE))
                .andRespond(withResourceNotFound());
        Mockito.doReturn(false).when(minioClient).bucketExists(Mockito.any(BucketExistsArgs.class));

        // Act
        appsService.deleteAppById(appUnderTest.getId().toString());

        // Assert
        final Optional<App> appOptional = appRepository.findById(appUnderTest.getId());
        Assertions.assertFalse(appOptional.isPresent());
    }

    @Test
    public void testDeleteAppById_failed_error_deleteAutomationCompositionType_call() throws Exception {
        // Arrange
        final App appUnderTest = appRepository.save(TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED));
        final String url = acmUrlGenerator.getAcmCompositionUrl() + "/" + appUnderTest.getCompositionId();

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE)).andRespond(withServerError());

        // Act
        final AppLcmException exception = Assertions.assertThrows(AppLcmException.class, () -> appsService.deleteAppById(appUnderTest.getId().toString()));

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        final Optional<App> appOptional = appRepository.findById(appUnderTest.getId());
        Assertions.assertTrue(appOptional.isPresent());
        Assertions.assertEquals(AppStatus.DELETE_ERROR, appOptional.get().getStatus());
    }

    @Test
    public void testDeleteAppById_failed_error_minio_bucketExist_call() throws Exception {
        // Arrange
        final App appUnderTest = appRepository.save(TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED));
        this.mockAcmCompositionCalls(appUnderTest);
        Mockito.doThrow(new IOException("Error transferring bucketId")).when(minioClient).bucketExists(Mockito.any(BucketExistsArgs.class));

        // Act
        final AppLcmException exception = Assertions.assertThrows(AppLcmException.class, () -> appsService.deleteAppById(appUnderTest.getId().toString()));

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        final Optional<App> appOptional = appRepository.findById(appUnderTest.getId());
        Assertions.assertTrue(appOptional.isPresent());
        Assertions.assertEquals(AppStatus.DELETE_ERROR, appOptional.get().getStatus());
    }

    @Test
    public void testDeleteAppById_success_minio_bucket_not_exist() throws Exception {
        // Arrange
        final App appUnderTest = appRepository.save(TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED));
        Mockito.doReturn(false).when(minioClient).bucketExists(Mockito.any(BucketExistsArgs.class));
        this.mockAcmCompositionCalls(appUnderTest);

        // Act
        Assertions.assertDoesNotThrow(() -> appsService.deleteAppById(appUnderTest.getId().toString()));

        // Assert
        final Optional<App> appOptional = appRepository.findById(appUnderTest.getId());
        Assertions.assertFalse(appOptional.isPresent());
    }

    @Test
    public void testDeleteAppById_success_minio_object_exist() throws Exception {
        // Arrange
        final App appUnderTest = appRepository.save(TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED));
        this.mockAcmCompositionCalls(appUnderTest);

        final Item mockItem = Mockito.mock(Item.class);
        final Result<Item> mockResult = Mockito.mock(Result.class);
        final Iterable<Result<Item>> mockIterable = Arrays.asList(mockResult);

        Mockito.doReturn(true).when(minioClient).bucketExists(Mockito.any(BucketExistsArgs.class));
        Mockito.doReturn(mockIterable).when(minioClient).listObjects(Mockito.any(ListObjectsArgs.class));
        Mockito.doReturn(mockItem).when(mockResult).get();
        Mockito.doReturn(TEST_STRING).when(mockItem).objectName();
        Mockito.doNothing().when(minioClient).removeObject(Mockito.any(RemoveObjectArgs.class));

        // Act
        Assertions.assertDoesNotThrow(() -> appsService.deleteAppById(appUnderTest.getId().toString()));

        // Assert
        final Optional<App> appOptional = appRepository.findById(appUnderTest.getId());
        Assertions.assertFalse(appOptional.isPresent());
    }

    @Test
    public void testDeleteAppById_failed_error_minio_listObject_call() throws Exception {
        // Arrange
        final App appUnderTest = appRepository.save(TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED));
        this.mockAcmCompositionCalls(appUnderTest);
        Mockito.doReturn(true).when(minioClient).bucketExists(Mockito.any(BucketExistsArgs.class));
        Mockito.doThrow(new RuntimeException("Error listing objects")).when(minioClient).listObjects(Mockito.any(ListObjectsArgs.class));

        // Act
        final AppLcmException exception = Assertions.assertThrows(AppLcmException.class, () -> appsService.deleteAppById(appUnderTest.getId().toString()));

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        final Optional<App> appOptional = appRepository.findById(appUnderTest.getId());
        Assertions.assertTrue(appOptional.isPresent());
        Assertions.assertEquals(AppStatus.DELETE_ERROR, appOptional.get().getStatus());
    }

    @Test
    public void testDeleteAppById_failed_error_minio_removeObject_call() throws Exception {
        // Arrange
        final App appUnderTest = appRepository.save(TestUtils.generateAppEntity(AppMode.DISABLED, AppStatus.CREATED));
        this.mockAcmCompositionCalls(appUnderTest);

        final Iterable<Result<Item>> mockIterable = Mockito.mock(Iterable.class);
        final Iterator<Result<Item>> mockIterator = Mockito.mock(Iterator.class);
        final Result<Item> mockResult = Mockito.mock(Result.class);
        final Item mockItem = Mockito.mock(Item.class);

        Mockito.doReturn(true).when(minioClient).bucketExists(Mockito.any(BucketExistsArgs.class));
        Mockito.doReturn(mockIterable).when(minioClient).listObjects(Mockito.any(ListObjectsArgs.class));
        Mockito.doReturn(mockItem).when(mockResult).get();
        Mockito.doReturn(TEST_STRING).when(mockItem).objectName();
        Mockito.doThrow(new IOException("Error listing objects")).when(minioClient).removeObject(Mockito.any(RemoveObjectArgs.class));
        Mockito.when(mockIterable.iterator()).thenReturn(mockIterator);
        Mockito.when(mockIterator.hasNext()).thenReturn(true);
        Mockito.when(mockIterator.next()).thenReturn(mockResult);

        // Act
        final AppLcmException exception = Assertions.assertThrows(AppLcmException.class, () -> appsService.deleteAppById(appUnderTest.getId().toString()));

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        final Optional<App> appOptional = appRepository.findById(appUnderTest.getId());
        Assertions.assertTrue(appOptional.isPresent());
        Assertions.assertEquals(AppStatus.DELETE_ERROR, appOptional.get().getStatus());
    }

    private void mockAcmCompositionCalls(App appUnderTest) throws IOException {
        final String url = acmUrlGenerator.getAcmCompositionUrl() + "/" + appUnderTest.getCompositionId();

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess(getClasspathResourceAsString(AUTOMATION_COMPOSITION_DEFINITION_JSON),
                        MediaType.APPLICATION_JSON));
    }
}