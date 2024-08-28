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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.core.services.AppInstancesService;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppEvent;
import com.ericsson.oss.ae.acm.persistence.entity.EventType;
import com.ericsson.oss.ae.acm.persistence.repository.AppEventRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class, AcmService.class, AppInstancesService.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class AppEventRepositoryTest {

    private App appUnderTest;

    private AppEvent appEventTest;

    @Autowired
    AppRepository appRepositoryTest;

    @Autowired
    AppEventRepository appEventRepositoryTest;

    @Autowired
    AppInstancesService appsInstanceService;

    @BeforeEach
    public void setUp() {
        appEventRepositoryTest.deleteAll();
        appRepositoryTest.deleteAll();

        appUnderTest = TestUtils.generateAppEntity();

        appUnderTest = appRepositoryTest.save(appUnderTest);

        appEventTest = AppEvent.builder().title("Failed to Enable app")
                .detail("PRIME request in ACM failed due to an authorization error").type(EventType.ERROR).app(appUnderTest).build();
    }

    @Test
    public void testSave() {
        AppEvent appEvent = appEventRepositoryTest.save(appEventTest);
        assertThat(appEvent).isNotNull();
        assertThat(appEvent.getId()).isNotNull();
        assertThat(appEvent.getApp()).isNotNull();
    }

    @Test
    public void testFIndById() {

        AppEvent appEvent = appEventRepositoryTest.save(appEventTest);
        assertThat(appEvent).isNotNull();
        Optional<AppEvent> appEvent1 = appEventRepositoryTest.findById(appEvent.getId());
        assertThat(appEvent1.isEmpty()).isFalse();
        assertThat(appEvent1.get().getId()).isNotNull();
        assertThat(appEvent1.get().getApp()).isNotNull();
        assertThat(appEvent1.get().getApp().getId()).isNotNull();
        assertThat(appEvent1.get().getTitle()).isEqualTo("Failed to Enable app");
        assertThat(appEvent1.get().getDetail()).isEqualTo("PRIME request in ACM failed due to an authorization error");
        assertThat(appEvent1.get().getType()).isEqualTo(EventType.ERROR);
        assertThat(appEvent1.get().getCreatedAt()).isNotNull();
    }

    @Test
    public void testFIndAll() {
        AppEvent appEvent = appEventRepositoryTest.save(appEventTest);
        assertThat(appEvent).isNotNull();
        List<AppEvent> appEvents = appEventRepositoryTest.findAll();
        assertThat(appEvents.size()).isEqualTo(1);
        assertThat(appEvents.get(0).getId()).isNotNull();
        assertThat(appEvents.get(0).getApp()).isNotNull();
        assertThat(appEvents.get(0).getApp().getId()).isNotNull();
        assertThat(appEvents.get(0).getTitle()).isEqualTo("Failed to Enable app");
        assertThat(appEvents.get(0).getDetail()).isEqualTo("PRIME request in ACM failed due to an authorization error");
        assertThat(appEvents.get(0).getType()).isEqualTo(EventType.ERROR);
        assertThat(appEvents.get(0).getCreatedAt()).isNotNull();
    }

    @Test
    public void testDelete() {
        AppEvent appEvent = appEventRepositoryTest.save(appEventTest);
        assertThat(appEvent).isNotNull();
        List<AppEvent> appEvents = appEventRepositoryTest.findAll();
        assertThat(appEvents.size()).isEqualTo(1);
        appEventRepositoryTest.deleteById(appEvent.getId());
        appEvents = appEventRepositoryTest.findAll();
        assertThat(appEvents.size()).isEqualTo(0);
    }

}
