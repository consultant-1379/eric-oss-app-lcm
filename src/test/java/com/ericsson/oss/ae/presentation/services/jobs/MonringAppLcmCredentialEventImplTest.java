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

package com.ericsson.oss.ae.presentation.services.jobs;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.clients.keycloak.KeycloakClient;
import com.ericsson.oss.ae.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.CredentialEvent;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.AppOnboardingAppNotExistException;
import com.ericsson.oss.ae.presentation.exceptions.ResourceNotFoundException;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import com.ericsson.oss.ae.repositories.CredentialEventRepository;
import groovy.util.logging.Slf4j;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { AppLcmApplication.class, MonitoringAppLcmCredentialEventImpl.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = { "file:src/test/resources/sql/monitoring/credential_event_data.sql" })
public class MonringAppLcmCredentialEventImplTest {

    @Autowired
    private MonitoringAppLcmCredentialEventImpl monitoringAppLcmCredentialEvent;

    @Autowired
    private CredentialEventRepository credentialEventRepository;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private AppInstanceRepository appInstancerepository;

    private MockMvc mvc;

    private MockRestServiceServer mockServer;

    @MockBean
    private KeycloakClient keycloakClient;

    @BeforeAll
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    public void resetMockServer() {
        mockServer.reset();
    }

    @Transactional
    @Test
    void givenPendingDeletionInstanceStatusCredentialEvent_WhenSameScope_ThenRecordAreDeleted() {
        when(keycloakClient.getClients()).thenReturn(ResponseEntity.ok(createClientDtoListHelper()));
        when(keycloakClient.deleteClient(any())).thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));
        List<AppInstance> listHelper = createAppInstanceListHelper();
        when(appInstancerepository.findAll()).thenReturn(listHelper);
        when(appInstancerepository.findById(any())).thenReturn(java.util.Optional.ofNullable(listHelper.get(2)));
        List<CredentialEvent> credentialEventListToDelete = getCredentialEventListRecordToDelete();

        BDDAssertions.then(credentialEventListToDelete.size()).isEqualTo(3);
        monitoringAppLcmCredentialEvent.execute();

        BDDAssertions.then(getCredentialEventListRecordToDelete().size()).isEqualTo(0);
        verify(keycloakClient, times(2)).deleteClient(any());
    }

    @Transactional
    @Test
    void givenPendingDeletionInstanceStatusCredentialEventWithoutAppInstanceId_WhenExecuteMonitoringJob_ThenRecordAreDeleted() {
        when(keycloakClient.getClients()).thenReturn(ResponseEntity.ok(createClientDtoListHelper()));
        when(keycloakClient.deleteClient(any())).thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));
        List<AppInstance> listHelper = createAppInstanceListHelper();
        when(appInstancerepository.findAll()).thenReturn(listHelper);
        when(appInstancerepository.findById(any())).thenReturn(java.util.Optional.ofNullable(null));
        List<CredentialEvent> credentialEventListToDelete = getCredentialEventListRecordToDelete();
        credentialEventListToDelete.get(0).setAppInstanceId(null);

        BDDAssertions.then(credentialEventListToDelete.size()).isEqualTo(3);
        monitoringAppLcmCredentialEvent.execute();

        BDDAssertions.then(getCredentialEventListRecordToDelete().size()).isEqualTo(0);
        verify(keycloakClient, times(2)).deleteClient(any());
    }

    @Transactional
    @Test
    void givenPendingDeletionInstanceStatusCredentialEvent_WhenKeyCloakNotResponse_ThenThrowException() {
        when(keycloakClient.getClients()).thenReturn(null);
        when(keycloakClient.deleteClient(any())).thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));
        List<AppInstance> listHelper = createAppInstanceListHelper();
        when(appInstancerepository.findAll()).thenReturn(listHelper);
        when(appInstancerepository.findById(any())).thenReturn(java.util.Optional.ofNullable(listHelper.get(0)));
        List<CredentialEvent> credentialEventListToDelete = getCredentialEventListRecordToDelete();

        BDDAssertions.then(credentialEventListToDelete.size()).isEqualTo(3);
        final ResourceNotFoundException actualException = assertThrows(ResourceNotFoundException.class, () -> {
            monitoringAppLcmCredentialEvent.execute();
        });


        BDDAssertions.then(getCredentialEventListRecordToDelete().size()).isEqualTo(3);
        verify(keycloakClient, times(0)).deleteClient(any());
        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.FAILURE_TO_RETRIEVE_ID_FROM_CLIENT);
    }


    private List<CredentialEvent> getCredentialEventListRecordToDelete() {
        List<CredentialEvent> credentialEventRepositoryList = credentialEventRepository.findAll();

        return credentialEventRepositoryList.stream().filter(e -> e.getDeletionStatus().equals("PENDING_DELETION")).collect(Collectors.toList());
    }

    private List<AppInstance> createAppInstanceListHelper(){
        List<AppInstance> appInstanceList = new ArrayList<>();
        AppInstance appInstance = new AppInstance();
        appInstance.setId(1L);
        appInstance.setAppOnBoardingAppId(1L);
        appInstance.setHealthStatus(HealthStatus.TERMINATED);
        appInstance.setTargetStatus(TargetStatus.TERMINATED);
        AppInstance appInstance2 = new AppInstance();
        appInstance2.setId(2L);
        appInstance2.setAppOnBoardingAppId(2L);
        appInstance2.setHealthStatus(HealthStatus.TERMINATED);
        appInstance2.setTargetStatus(TargetStatus.TERMINATED);
        AppInstance appInstance3 = new AppInstance();
        appInstance3.setId(3L);
        appInstance3.setAppOnBoardingAppId(3L);
        appInstance3.setHealthStatus(HealthStatus.TERMINATED);
        appInstance3.setTargetStatus(TargetStatus.TERMINATED);
        appInstanceList.add(appInstance);
        appInstanceList.add(appInstance2);
        appInstanceList.add(appInstance3);
        return appInstanceList;
    }

    private ClientDto[] createClientDtoListHelper(){
        ClientDto client1 = new ClientDto();
        client1.setClientId("rApp_73c897540dfd");
        client1.setName("scope_test");
        client1.setId("1");
        ClientDto client2 = new ClientDto();
        client2.setClientId("rApp_73c897540dfd");
        client2.setName("scope_test_delete");
        client2.setId("1");
        return new ClientDto[] {client1, client2};
    }


}
