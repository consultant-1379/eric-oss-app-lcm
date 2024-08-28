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

package com.ericsson.oss.ae.presentation.services;

import com.ericsson.oss.ae.model.AppInstanceFilter;
import com.ericsson.oss.ae.model.entity.AppInstance;
import com.ericsson.oss.ae.model.entity.ArtifactInstance;
import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import com.ericsson.oss.ae.repositories.AppInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Transactional
class AppInstanceJpaSpecificationTest {

    @Autowired
    private AppInstanceJpaSpecification specification;
    @Autowired
    private AppInstanceRepository repository;

    @BeforeEach
    void init(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final HealthStatus terminated = HealthStatus.TERMINATED;
        AppInstance appInstance = getAppInstance(appId, appInstanceId, workloadId, terminated);
        repository.save(appInstance);
    }

    @Test
    void givenAllItemsFromFiltersForSpecification(){
        final Long appId = 1L;
        final Long appInstanceId = 2L;
        final Long workloadId = 3L;
        final HealthStatus terminated = HealthStatus.TERMINATED;

        final AppInstanceFilter filter = new AppInstanceFilter();
        filter.setInstanceIds(Arrays.asList(appInstanceId));
        filter.setAppOnBoardingAppId(Arrays.asList(appId));
        filter.setAdditionalParameters(Arrays.asList("Additional parameters test"));
        filter.setHealthStatus(Arrays.asList(terminated));
        filter.setTargetStatus(Arrays.asList(TargetStatus.TERMINATED));
        filter.setCreateTimeStamp(Arrays.asList(ZonedDateTime.now()));
        filter.setUpdateTimeStamp(Arrays.asList(ZonedDateTime.now()));


        final List<AppInstance> objectToTest = repository
            .findAll(specification.getAppInstanceRequest(filter));


        assertEquals(0, objectToTest.size());

    }



    private AppInstance getAppInstance(Long appId, Long appInstanceId, Long workloadId,
                                       HealthStatus healthStatus) {
        String workloadStringId = workloadId != null?
            workloadId.toString() : null;
        final ArtifactInstance artifactInstance = ArtifactInstance.builder()
            .id(2L).workloadInstanceId(workloadStringId).build();
        artifactInstance.setHealthStatus(healthStatus);
        final AppInstance appInstance = AppInstance.builder().id(appInstanceId)
            .appOnBoardingAppId(appId).build();
        appInstance.setHealthStatus(healthStatus);
        appInstance.setArtifactInstances(Collections.singletonList(artifactInstance));
        return appInstance;
    }
}