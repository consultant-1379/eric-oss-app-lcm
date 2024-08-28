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
import static com.ericsson.oss.ae.acm.TestConstants.GLOBAL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.ericsson.oss.ae.acm.persistence.entity.AppInstanceEvent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstanceEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class AppInstanceEventRepositoryTest {

    private App appUnderTest;

    private AppInstances appInstancesTest;

    private ClientCredential credentialTest;

    private AppInstanceEvent appInstanceEventTest;

    @Autowired
    AppInstancesRepository appInstancesRepositoryTest;

    @Autowired
    AppRepository appRepositoryTest;

    @Autowired
    AppInstanceEventRepository appInstanceEventRepositoryTest;

    @BeforeEach
    public void setUp() {

        appUnderTest = TestUtils.generateAppEntity();

        appUnderTest = appRepositoryTest.save(appUnderTest);

        appInstancesTest = AppInstances.builder().app(appUnderTest).compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED)
                .status(AppInstanceStatus.UNDEPLOYED).build();

        credentialTest = ClientCredential.builder().clientId("1234")
                .clientScope(GLOBAL).clientSecret("@$#baelDunG@#^$*").appInstance(appInstancesTest).build();

        // Create a UUID for the AppInstance
        final UUID appInstanceId = UUID.randomUUID();
        appInstancesTest.setId(appInstanceId);

        appInstancesTest.setClientCredentials(List.of(credentialTest));

        appInstancesRepositoryTest.save(appInstancesTest);

        appInstanceEventTest = AppInstanceEvent.builder().title("Failed to deploy app").detail("Failed to deploy app at HFE").type(EventType.ERROR)
                .appInstance(appInstancesTest).build();
    }

    @Test
    @Transactional
    public void testSave() {
        AppInstanceEvent instanceEvent = appInstanceEventRepositoryTest.save(appInstanceEventTest);
        assertThat(instanceEvent).isNotNull();
        assertThat(instanceEvent.getId()).isNotNull();
        assertThat(instanceEvent.getAppInstance()).isNotNull();
    }

    @Test
    @Transactional
    public void testFIndById() {

        AppInstanceEvent instanceEvent = appInstanceEventRepositoryTest.save(appInstanceEventTest);
        assertThat(instanceEvent).isNotNull();
        Optional<AppInstanceEvent> appInstanceEvent1 = appInstanceEventRepositoryTest.findById(instanceEvent.getId());
        assertThat(appInstanceEvent1.isEmpty()).isFalse();
        assertThat(appInstanceEvent1.get().getId()).isNotNull();
        assertThat(appInstanceEvent1.get().getAppInstance()).isNotNull();
        assertThat(appInstanceEvent1.get().getAppInstance().getId()).isNotNull();
        assertThat(appInstanceEvent1.get().getTitle()).isEqualTo("Failed to deploy app");
        assertThat(appInstanceEvent1.get().getDetail()).isEqualTo("Failed to deploy app at HFE");
        assertThat(appInstanceEvent1.get().getType()).isEqualTo(EventType.ERROR);
        assertThat(appInstanceEvent1.get().getCreatedAt()).isNotNull();
    }

    @Test
    @Transactional
    public void testFIndAll() {
        AppInstanceEvent instanceEvent = appInstanceEventRepositoryTest.save(appInstanceEventTest);
        assertThat(instanceEvent).isNotNull();
        List<AppInstanceEvent> appInstanceEvents = appInstanceEventRepositoryTest.findAll();
        assertThat(appInstanceEvents.size()).isEqualTo(1);
        assertThat(appInstanceEvents.get(0).getId()).isNotNull();
        assertThat(appInstanceEvents.get(0).getAppInstance()).isNotNull();
        assertThat(appInstanceEvents.get(0).getAppInstance().getId()).isNotNull();
        assertThat(appInstanceEvents.get(0).getTitle()).isEqualTo("Failed to deploy app");
        assertThat(appInstanceEvents.get(0).getDetail()).isEqualTo("Failed to deploy app at HFE");
        assertThat(appInstanceEvents.get(0).getType()).isEqualTo(EventType.ERROR);
        assertThat(appInstanceEvents.get(0).getCreatedAt()).isNotNull();
    }

    @Test
    @Transactional
    public void testDelete() {
        AppInstanceEvent instanceEvent = appInstanceEventRepositoryTest.save(appInstanceEventTest);
        assertThat(instanceEvent).isNotNull();
        List<AppInstanceEvent> appInstanceEvents = appInstanceEventRepositoryTest.findAll();
        assertThat(appInstanceEvents.size()).isEqualTo(1);
        appInstanceEventRepositoryTest.deleteById(instanceEvent.getId());
        appInstanceEvents = appInstanceEventRepositoryTest.findAll();
        assertThat(appInstanceEvents.size()).isEqualTo(0);
    }

}
