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

import static com.ericsson.oss.ae.acm.TestConstants.ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED;
import static com.ericsson.oss.ae.acm.TestConstants.COMPOSITION_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class AppInstancesRepositoryTest {

    private App appUnderTest;

    private AppInstances appInstancesTest;

    @Autowired
    AppInstancesRepository appInstancesRepository;

    @Autowired
    AppRepository appRepositoryTest;

    @BeforeEach
    public void setUp() {

        appUnderTest = TestUtils.generateAppEntity();

        appUnderTest = appRepositoryTest.save(appUnderTest);

        appInstancesTest = AppInstances.builder()
            .id(UUID.randomUUID())
            .app(appUnderTest)
            .compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED)
            .status(AppInstanceStatus.UNDEPLOYED).build();
    }

    @Test
    @Transactional
    public void testSave() {
        AppInstances appInstances = appInstancesRepository.save(appInstancesTest);
        assertThat(appInstances).isNotNull();
        assertThat(appInstances.getId()).isNotNull();
        assertThat(appInstances.getApp()).isNotNull();
    }

    @Test
    @Transactional
    public void testFindById() {
        AppInstances appInstance = appInstancesRepository.save(appInstancesTest);
        assertThat(appInstance).isNotNull();
        Optional<AppInstances> appInstances = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstances.isEmpty()).isFalse();
        assertThat(appInstances.get().getId()).isNotNull();
        assertThat(appInstances.get().getCompositionInstanceId()).isEqualTo(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED);
        assertThat(appInstances.get().getStatus()).isEqualTo(AppInstanceStatus.UNDEPLOYED);
        assertThat(appInstances.get().getApp().getId()).isEqualTo(appUnderTest.getId());
        assertThat(appInstances.get().getApp().getCompositionId()).isEqualTo(COMPOSITION_ID);
        assertThat(appInstances.get().getApp().getAppComponents().size()).isEqualTo(1);
    }

    @Test
    @Transactional
    public void testFindAll() {
        AppInstances appInstance = appInstancesRepository.save(appInstancesTest);
        assertThat(appInstance).isNotNull();
        List<AppInstances> appInstances = appInstancesRepository.findAll();
        assertThat(appInstances.isEmpty()).isFalse();
        assertThat(appInstances.size()).isEqualTo(1);
        assertThat(appInstances.get(0).getId()).isNotNull();
        assertThat(appInstances.get(0).getCompositionInstanceId()).isEqualTo(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED);
        assertThat(appInstances.get(0).getStatus()).isEqualTo(AppInstanceStatus.UNDEPLOYED);
        assertThat(appInstances.get(0).getApp().getId()).isEqualTo(appUnderTest.getId());
        assertThat(appInstances.get(0).getApp().getCompositionId()).isEqualTo(COMPOSITION_ID);
    }

    @Test
    @Transactional
    public void testUpdate() {
        ClientCredential credential = ClientCredential.builder().appInstance(appInstancesTest).build();
        appInstancesTest.setClientCredentials(List.of(credential));
        AppInstances appInstance = appInstancesRepository.save(appInstancesTest);
        assertThat(appInstance).isNotNull();
        appInstance.setStatus(AppInstanceStatus.DEPLOYING);
        appInstance = appInstancesRepository.save(appInstance);
        Optional<AppInstances> appInstances = appInstancesRepository.findById(appInstance.getId());
        assertThat(appInstances.isEmpty()).isFalse();
        assertThat(appInstances.get().getId()).isNotNull();
        assertThat(appInstances.get().getCompositionInstanceId()).isEqualTo(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED);
        assertThat(appInstances.get().getStatus()).isEqualTo(AppInstanceStatus.DEPLOYING);
    }

    @Test
    @Transactional
    public void testDelete() {
        AppInstances appInstance = appInstancesRepository.save(appInstancesTest);
        assertThat(appInstance).isNotNull();
        List<AppInstances> appInstances = appInstancesRepository.findAll();
        assertThat(appInstances.size()).isEqualTo(1);
        appInstancesRepository.deleteById(appInstance.getId());
        appInstances = appInstancesRepository.findAll();
        assertThat(appInstances.size()).isEqualTo(0);
    }

//    @Test
//    @Transactional
//    public void testFindAllByAppId() {
//        App appUnderTestX = TestUtils.generateAppEntity();
//        appUnderTestX = appRepositoryTest.save(appUnderTestX);
//
//        appInstancesRepository.save(appInstancesTest);
//        appInstancesRepository.save(
//                AppInstances.builder().app(appUnderTest).compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED).status(AppInstanceStatus.DEPLOYED)
//                        .build());
//
//
//        appInstancesRepository.save(appInstancesTest);
//        appInstancesRepository.save(
//                AppInstances.builder().app(appUnderTestX).compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED).status(AppInstanceStatus.DEPLOY_ERROR)
//                        .build());
//
//        List<AppInstances> appInstances = appInstancesRepository.findAllByAppId(appUnderTest.getId());
//        Assertions.assertFalse(appInstances.isEmpty());
//        Assertions.assertEquals(2, appInstances.size());
//        Assertions.assertEquals(appUnderTest.getId(), appInstances.get(0).getApp().getId());
//        Assertions.assertEquals(appUnderTest.getId(), appInstances.get(1).getApp().getId());
//    }

    @Test
    @Transactional
    public void testFindAllByAppId() {
        App appUnderTest = TestUtils.generateAppEntity();
        appUnderTest = appRepositoryTest.save(appUnderTest);

        AppInstances appInstancesTest = AppInstances.builder()
            .id(UUID.randomUUID())
            .app(appUnderTest)
            .compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED)
            .status(AppInstanceStatus.DEPLOY_ERROR)
            .build();
        appInstancesRepository.save(appInstancesTest);

        // Creating another entity with a different ID
        AppInstances appInstancesAnotherTest = AppInstances.builder()
            .id(UUID.randomUUID())
            .app(appUnderTest)
            .compositionInstanceId(UUID.randomUUID())
            .status(AppInstanceStatus.DEPLOYED)
            .build();
        appInstancesRepository.save(appInstancesAnotherTest);

        // Retrieve and assert
        List<AppInstances> appInstances = appInstancesRepository.findAllByAppId(appUnderTest.getId());

        Assertions.assertFalse(appInstances.isEmpty());
        Assertions.assertEquals(2, appInstances.size());
        Assertions.assertEquals(appUnderTest.getId(), appInstances.get(0).getApp().getId());
        Assertions.assertEquals(appUnderTest.getId(), appInstances.get(1).getApp().getId());
    }
}