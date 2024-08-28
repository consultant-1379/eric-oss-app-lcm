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
import static com.ericsson.oss.ae.acm.TestConstants.REPLICA_COUNT_KEY;
import static com.ericsson.oss.ae.acm.TestConstants.REPLICA_COUNT_VALUE;
import static com.ericsson.oss.ae.constants.KeycloakConstants.CLIENT_ID_NAME;
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
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;
import com.ericsson.oss.ae.acm.persistence.repository.AppInstancesRepository;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.acm.persistence.repository.ClientCredentialRepository;
import com.ericsson.oss.ae.v3.api.model.AppInstanceStatus;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AppLcmApplication.class})
@TestPropertySource(locations = "classpath:acm/application-test.properties")
public class ClientCredentialTest {

    private App appUnderTest;

    private AppInstances appInstancesTest;

    private ClientCredential credentialTest;

    @Autowired
    AppInstancesRepository appInstancesRepositoryTest;

    @Autowired
    AppRepository appRepositoryTest;

    @Autowired
    ClientCredentialRepository credentialRepositoryTest;

    @BeforeEach
    public void setUp() {

        appUnderTest = TestUtils.generateAppEntity();

        appUnderTest = appRepositoryTest.save(appUnderTest);

        appInstancesTest = AppInstances.builder().app(appUnderTest).compositionInstanceId(ACM_COMPOSITION_INSTANCE_ID_UNDEPLOYED)
                .status(AppInstanceStatus.UNDEPLOYED).build();

        // Create a UUID for the AppInstance
        final UUID appInstanceId = UUID.randomUUID();
        appInstancesTest.setId(appInstanceId);

        appInstancesRepositoryTest.save(appInstancesTest);

        credentialTest = ClientCredential.builder().clientId(CLIENT_ID_NAME)
                .clientScope(GLOBAL).clientSecret("@$#baelDunG@#^$*").appInstance(appInstancesTest).build();
    }

    @Test
    @Transactional
    public void testSave() {
        ClientCredential credential = credentialRepositoryTest.save(credentialTest);
        assertThat(credential).isNotNull();
        assertThat(credential.getId()).isNotNull();
        assertThat(credential.getAppInstance()).isNotNull();
    }

    @Test
    @Transactional
    public void testFindById() {
        ClientCredential credential = credentialRepositoryTest.save(credentialTest);
        Optional<ClientCredential> credentials = credentialRepositoryTest.findById(credential.getId());
        assertThat(credentials.isEmpty()).isFalse();
        assertThat(credentials.get().getId()).isNotNull();
        assertThat(credentials.get().getAppInstance()).isNotNull();
        assertThat(credentials.get().getAppInstance().getId()).isNotNull();
        assertThat(credentials.get().getClientId()).isEqualTo(CLIENT_ID_NAME);
        assertThat(credentials.get().getClientSecret()).isEqualTo("@$#baelDunG@#^$*");
        assertThat(credentials.get().getClientScope()).isEqualTo(GLOBAL);
    }

    @Test
    @Transactional
    public void testFindAll() {
        ClientCredential credential = credentialRepositoryTest.save(credentialTest);
        List<ClientCredential> credentials = credentialRepositoryTest.findAll();
        assertThat(credentials.size()).isEqualTo(1);
        assertThat(credentials.get(0).getId()).isNotNull();
        assertThat(credentials.get(0).getAppInstance()).isNotNull();
        assertThat(credentials.get(0).getAppInstance().getId()).isNotNull();
        assertThat(credentials.get(0).getClientId()).isEqualTo(CLIENT_ID_NAME);
        assertThat(credentials.get(0).getClientSecret()).isEqualTo("@$#baelDunG@#^$*");
        assertThat(credentials.get(0).getClientScope()).isEqualTo(GLOBAL);

    }

    @Test
    @Transactional
    public void testDelete() {
        ClientCredential credential = credentialRepositoryTest.save(credentialTest);
        List<ClientCredential> credentials = credentialRepositoryTest.findAll();
        assertThat(credentials.size()).isEqualTo(1);
        credentialRepositoryTest.deleteById(credential.getId());
        credentials = credentialRepositoryTest.findAll();
        assertThat(credentials.size()).isEqualTo(0);
    }

}
