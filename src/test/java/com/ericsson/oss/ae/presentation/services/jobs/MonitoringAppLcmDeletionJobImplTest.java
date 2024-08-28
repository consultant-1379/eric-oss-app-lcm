/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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
import com.ericsson.oss.ae.clients.apponboarding.AppOnboardingClient;
import com.ericsson.oss.ae.clients.helmorchestrator.HelmOrchestratorClient;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.presentation.exceptions.AppOnBoardingDeleteException;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import groovy.util.logging.Slf4j;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ON_BOARDING_DELETE_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { AppLcmApplication.class, MonitoringAppLcmDeletionJobImpl.class })
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Sql(scripts = { "file:src/test/resources/sql/monitoring/monitoring_deleted_data.sql" })
class MonitoringAppLcmDeletionJobImplTest {

    @Autowired
    private MonitoringAppLcmDeletionJobImpl lcmDeletionJob;
    @Autowired
    private AppInstanceRepository repository;
    @MockBean
    private HelmOrchestratorClient helmOrchestratorClient;
    @MockBean
    private AppOnboardingClient appOnboardingClient;

    @Transactional
    @Test
    void givenListWithInvalidAndValidInstances_WhenGetDeletingInstances_FindTwoValidToDeleteRecords(){
        //find all instances - the sql script has 13 records
        List<AppInstance> allInstances = repository.findAll();
        BDDAssertions.then(allInstances).hasSize(13);
        //find only valid instances, one Target Status DELETED and Health Status DELETING and another one Target Status DELETED and Health Status FAILED
        List<AppInstance> validAppInstanceList = lcmDeletionJob.getDeletingInstances();
        BDDAssertions.then(validAppInstanceList).isNotEmpty();
        BDDAssertions.then(validAppInstanceList).hasSize(2);
        BDDAssertions.then(validAppInstanceList.get(0).getTargetStatus()).isEqualTo(TargetStatus.APP_DELETED);
        BDDAssertions.then(validAppInstanceList.get(1).getTargetStatus()).isEqualTo(TargetStatus.APP_DELETED);
        BDDAssertions.then(Arrays.asList(HealthStatus.DELETING, HealthStatus.FAILED).contains(validAppInstanceList.get(0).getHealthStatus()));
        BDDAssertions.then(Arrays.asList(HealthStatus.DELETING, HealthStatus.FAILED).contains(validAppInstanceList.get(1).getHealthStatus()));
    }

    @Transactional
    @Test
    void givenValidInstancesAndArtifacts_WhenExecutingDeletion_ThenRecordsAreDeleted() {
        assertThat(repository).isNotNull();

        when(helmOrchestratorClient.deleteWorkloadInstanceId(anyString()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));
        when(appOnboardingClient.deletePackage(anyLong()))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        List<AppInstance> validAppInstanceList = lcmDeletionJob.getDeletingInstances();
        BDDAssertions.then(validAppInstanceList.size()).isEqualTo(2);
        //run monitoring job to remove 2 valid instances
        lcmDeletionJob.execute();
        //after execution, should not been left any more valid instances to delete
        List<AppInstance> expectedEmptyList = lcmDeletionJob.getDeletingInstances();
        BDDAssertions.then(expectedEmptyList.size()).isEqualTo(0);
    }

    @Transactional
    @Test
    void givenInvalidRequest_WhenExecutingDeletion_ThenRecordsAreNotDeleted() {
        assertThat(repository).isNotNull();

        when(helmOrchestratorClient.deleteWorkloadInstanceId(anyString()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));
        when(appOnboardingClient.deletePackage(anyLong()))
            .thenThrow(new AppOnBoardingDeleteException(APP_ON_BOARDING_DELETE_ERROR, "Error deleting app"));

        List<AppInstance> validAppInstanceList = lcmDeletionJob.getDeletingInstances();
        BDDAssertions.then(validAppInstanceList.size()).isEqualTo(2);
        //run monitoring job and throw exception for deleting from onBoarding
        final AppOnBoardingDeleteException actualException = assertThrows(AppOnBoardingDeleteException.class, () -> {
            lcmDeletionJob.execute();
        });

        assertThat(actualException.getMessage()).isEqualTo("Error deleting app");
        assertThat(actualException.getAppLcmError()).isEqualTo(APP_ON_BOARDING_DELETE_ERROR);
    }

    @Transactional
    @Test
    void givenInvalidRequest_WhenExecutingDeletion_ThenRecordsAreNotDeletedAndResourceAccessExceptionCatched() {
        assertThat(repository).isNotNull();

        when(helmOrchestratorClient.deleteWorkloadInstanceId(anyString()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));
        when(appOnboardingClient.deletePackage(anyLong()))
            .thenThrow(new ResourceAccessException("test"));
        //find instances for deletion - it should be 2
        List<AppInstance> validAppInstanceList = lcmDeletionJob.getDeletingInstances();

        BDDAssertions.then(validAppInstanceList.size()).isEqualTo(2);
        //run monitoring job and catch RestClientException
        lcmDeletionJob.execute();
        //instances are not deleted so still should have 2 records
        BDDAssertions.then(validAppInstanceList.size()).isEqualTo(2);
    }

    @Transactional
    @Test
    void givenInvalidRequest_WhenExecutingDeletion_ThenRecordsAreNotDeletedAndRestClientExceptionCatched() {
        assertThat(repository).isNotNull();

        when(helmOrchestratorClient.deleteWorkloadInstanceId(anyString()))
            .thenReturn(new ResponseEntity<>(new HttpHeaders(), HttpStatus.NO_CONTENT));
        when(appOnboardingClient.deletePackage(anyLong()))
            .thenThrow(new RestClientException("test"));
        //find instances for deletion - it should be 2
        List<AppInstance> validAppInstanceList = lcmDeletionJob.getDeletingInstances();
        BDDAssertions.then(validAppInstanceList.size()).isEqualTo(2);
        //run monitoring job and catch RestClientException
        lcmDeletionJob.execute();
        //instances are not deleted so still should have 2 records
        BDDAssertions.then(validAppInstanceList.size()).isEqualTo(2);
    }
}