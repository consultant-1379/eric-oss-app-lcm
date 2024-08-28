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

package com.ericsson.oss.ae.acm.persistence.repositories;

import static com.ericsson.oss.ae.acm.TestConstants.ADMIN;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_NAME;
import static com.ericsson.oss.ae.acm.TestConstants.APP_ENTITY_VERSION;
import static com.ericsson.oss.ae.acm.TestConstants.ARTIFACT_VERSION;
import static com.ericsson.oss.ae.acm.TestConstants.COMPOSITION_ID;
import static com.ericsson.oss.ae.acm.TestConstants.DOCKER;
import static com.ericsson.oss.ae.acm.TestConstants.FOO;
import static com.ericsson.oss.ae.acm.TestConstants.GLOBAL;
import static com.ericsson.oss.ae.acm.TestConstants.HELLO_WORLD;
import static com.ericsson.oss.ae.acm.TestConstants.HELLO_WORLD_APP;
import static com.ericsson.oss.ae.acm.TestConstants.HELM;
import static com.ericsson.oss.ae.acm.TestConstants.IMAGE;
import static com.ericsson.oss.ae.acm.TestConstants.NGINX;
import static com.ericsson.oss.ae.acm.TestConstants.USER;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.AUTOMATION_COMPOSITION_ELEMENT;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.MICROSERVICE;
import static com.ericsson.oss.ae.constants.KeycloakConstants.KAFKA;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppMode;
import com.ericsson.oss.ae.v3.api.model.AppStatus;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class AppRepositoryTest {

    private App appUnderTest;

    @Autowired
    AppRepository appRepositoryTest;

    @BeforeEach
    public void setUp() {
        appUnderTest = TestUtils.generateAppEntity();
    }

    @AfterEach
    @BeforeAll
    public void cleanUp() {
        appRepositoryTest.deleteAll();
    }

    @Test
    public void testSave() {
        appRepositoryTest.save(appUnderTest);
        List<App> appRecords = appRepositoryTest.findAll();
        assertThat(appRecords.size()).isEqualTo(1);
        App appRecord = appRecords.get(0);
        assertThat(appRecord.getId()).isNotNull();

    }

    @Test
    public void testGetApps() {
        appRepositoryTest.save(appUnderTest);
        List<App> appRecords = appRepositoryTest.findAll();
        assertThat(appRecords.size()).isEqualTo(1);
        App appRecord = appRecords.get(0);
        assertThat(appRecord.getCompositionId()).isEqualTo(COMPOSITION_ID);
        assertThat(appRecord.getMode()).isEqualTo(AppMode.DISABLED);
        assertThat(appRecord.getStatus()).isEqualTo(AppStatus.CREATED);
        assertThat(appRecord.getName()).isEqualTo(APP_ENTITY_NAME);
        assertThat(appRecord.getVersion()).isEqualTo(APP_ENTITY_VERSION);

    }

    @Test
    public void testGetAppById() {
        App appRecord = appRepositoryTest.save(appUnderTest);
        Optional<App> appDetail = appRepositoryTest.findById(appRecord.getId());
        assertThat(appDetail.isEmpty()).isFalse();
        assertThat(appDetail.get().getCompositionId()).isEqualTo(COMPOSITION_ID);
        assertThat(appDetail.get().getMode()).isEqualTo(AppMode.DISABLED);
        assertThat(appDetail.get().getStatus()).isEqualTo(AppStatus.CREATED);
        assertThat(appDetail.get().getName()).isEqualTo(APP_ENTITY_NAME);
        assertThat(appDetail.get().getVersion()).isEqualTo(APP_ENTITY_VERSION);
        assertThat(appDetail.get().getCreatedAt()).isNotNull();
        assertThat(appDetail.get().getUpdatedAt()).isNotNull();
    }

    @Test
    public void testGetAppById_validateRoles() {
        App appRecord = appRepositoryTest.save(appUnderTest);
        Optional<App> appDetail = appRepositoryTest.findById(appRecord.getId());
        assertThat(appDetail.isEmpty()).isFalse();
        assertThat(appRecord.getRoles().size()).isEqualTo(2);
        assertThat(appRecord.getRoles().get(0).getId()).isNotNull();
        assertThat(appRecord.getRoles().get(0).getApp()).isNotNull();
        assertThat(appRecord.getRoles().get(0).getName()).isEqualTo(ADMIN);
        assertThat(appRecord.getRoles().get(1).getId()).isNotNull();
        assertThat(appRecord.getRoles().get(1).getApp()).isNotNull();
        assertThat(appRecord.getRoles().get(1).getName()).isEqualTo(USER);
    }

    @Test
    public void testGetAppById_validatePermissions() {
        App appRecord = appRepositoryTest.save(appUnderTest);
        Optional<App> appDetail = appRepositoryTest.findById(appRecord.getId());
        assertThat(appDetail.isEmpty()).isFalse();
        assertThat(appRecord.getPermissions().size()).isEqualTo(2);
        assertThat(appRecord.getPermissions().get(0).getId()).isNotNull();
        assertThat(appRecord.getPermissions().get(0).getApp()).isNotNull();
        assertThat(appRecord.getPermissions().get(0).getResource()).isEqualTo(KAFKA);
        assertThat(appRecord.getPermissions().get(0).getScope()).isEqualTo(GLOBAL);
        assertThat(appRecord.getPermissions().get(1).getId()).isNotNull();
        assertThat(appRecord.getPermissions().get(1).getApp()).isNotNull();
        assertThat(appRecord.getPermissions().get(1).getResource()).isEqualTo(NGINX);
        assertThat(appRecord.getPermissions().get(1).getScope()).isEqualTo(FOO);
    }

    @Test
    public void testGetAppById_validateAppComponents() {
        App appRecord = appRepositoryTest.save(appUnderTest);
        Optional<App> appDetail = appRepositoryTest.findById(appRecord.getId());
        assertThat(appDetail.isEmpty()).isFalse();
        assertThat(appRecord.getAppComponents().size()).isEqualTo(1);
        assertThat(appRecord.getAppComponents().get(0).getId()).isNotNull();
        assertThat(appRecord.getAppComponents().get(0).getApp()).isNotNull();
        assertThat(appRecord.getAppComponents().get(0).getName()).isEqualTo(HELLO_WORLD_APP);
        assertThat(appRecord.getAppComponents().get(0).getType()).isEqualTo(MICROSERVICE);
        assertThat(appRecord.getAppComponents().get(0).getVersion()).isEqualTo(APP_ENTITY_VERSION);
        assertThat(appRecord.getAppComponents().get(0).getCompositionElementName()).isEqualTo(AUTOMATION_COMPOSITION_ELEMENT);
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().size()).isEqualTo(2);
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(0).getId()).isNotNull();
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(0).getName()).isEqualTo(HELLO_WORLD);
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(0).getVersion()).isEqualTo(ARTIFACT_VERSION);
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(0).getType()).isEqualTo(HELM);
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(0).getAppComponent()).isNotNull();
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(1).getId()).isNotNull();
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(1).getName()).isEqualTo(DOCKER);
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(1).getVersion()).isEqualTo(ARTIFACT_VERSION);
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(1).getType()).isEqualTo(IMAGE);
        assertThat(appRecord.getAppComponents().get(0).getArtifacts().get(1).getAppComponent()).isNotNull();
    }

    @Test
    @Transactional
    public void testUpdate() {
        UUID appId = UUID.randomUUID();
        appUnderTest.setId(appId);
        appUnderTest = appRepositoryTest.save(appUnderTest);
        assertThat(appUnderTest.getMode()).isEqualTo(AppMode.DISABLED);
        appUnderTest.setMode(AppMode.ENABLED);
        appUnderTest = appRepositoryTest.save(appUnderTest);
        assertThat(appUnderTest.getMode()).isEqualTo(AppMode.ENABLED);
    }

    @Test
    public void testDelete() {
        appRepositoryTest.save(appUnderTest);
        List<App> appRecords = appRepositoryTest.findAll();
        assertThat(appRecords.size()).isEqualTo(1);
        appRepositoryTest.deleteById(appRecords.get(0).getId());
        appRecords = appRepositoryTest.findAll();
        assertThat(appRecords.size()).isEqualTo(0);
    }

}
