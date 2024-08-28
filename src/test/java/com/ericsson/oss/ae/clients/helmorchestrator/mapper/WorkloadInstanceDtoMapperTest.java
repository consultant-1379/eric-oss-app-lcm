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

package com.ericsson.oss.ae.clients.helmorchestrator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.api.model.AppInstancePostRequestDto;
import com.ericsson.oss.ae.clients.helmorchestrator.model.WorkloadInstance;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePostRequestDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePutRequestDto;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { WorkloadInstanceDtoMapper.class })
public class WorkloadInstanceDtoMapperTest {

    @Autowired
    private WorkloadInstanceDtoMapper objectUnderTest;

    @SpyBean
    private EnvironmentHolder  environmentHolder;

    @Test
    public void givenValidWorkloadInstanceDto_whenMappingToWorkloadInstance_thenWorkloadInstanceInstantiatedWithCorrectValues() {
        final WorkloadInstanceDto actualDtoObject = new WorkloadInstanceDto().workloadInstanceId("1").workloadInstanceName("workloadInstanceName")
                .namespace("appmanager-rapp").crdNamespace("namespace").cluster("localhost").helmSourceVersions(List.of("V1")).version(1)
                .additionalParameters(Collections.singletonMap("TEST_KEY", "TEST_VALUE"));
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        final WorkloadInstance actualMappedObject = objectUnderTest.map(actualDtoObject, WorkloadInstance.class);

        assertThat(actualMappedObject.getWorkloadInstanceId()).isEqualTo("1");
        assertThat(actualMappedObject.getWorkloadInstanceName()).isEqualTo("workloadInstanceName");
        assertThat(actualMappedObject.getNamespace()).isEqualTo("appmanager-rapp");
        assertThat(actualMappedObject.getCrdNamespace()).isEqualTo("namespace");
        assertThat(actualMappedObject.getCluster()).isEqualTo("localhost");
        assertThat(actualMappedObject.getHelmSourceVersions().get(0)).isEqualTo("V1");
        assertThat(actualMappedObject.getVersion()).isEqualTo(1);
        assertThat(actualMappedObject.getAdditionalParameters()).isEqualTo(Collections.singletonMap("TEST_KEY", "TEST_VALUE"));
    }

    @Test
    public void givenValidWorkloadInstance_whenMappingToWorkloadInstanceDto_thenWorkloadInstanceDtoInstantiatedWithCorrectValues() {
        final WorkloadInstance actualInstanceObject = WorkloadInstance.builder().workloadInstanceId("1").workloadInstanceName("workloadInstanceName")
                .namespace("appmanager-rapp").crdNamespace("namespace").cluster("localhost").helmSourceVersions(List.of("V1")).version(1)
                .additionalParameters(Collections.singletonMap("TEST_KEY", "TEST_VALUE")).build();
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        final WorkloadInstanceDto actualDtoObject = objectUnderTest.map(actualInstanceObject, WorkloadInstanceDto.class);

        assertThat(actualDtoObject.getWorkloadInstanceId()).isEqualTo("1");
        assertThat(actualDtoObject.getWorkloadInstanceName()).isEqualTo("workloadInstanceName");
        assertThat(actualDtoObject.getNamespace()).isEqualTo("appmanager-rapp");
        assertThat(actualDtoObject.getCrdNamespace()).isEqualTo("namespace");
        assertThat(actualDtoObject.getCluster()).isEqualTo("localhost");
        assertThat(actualDtoObject.getHelmSourceVersions().get(0)).isEqualTo("V1");
        assertThat(actualDtoObject.getVersion()).isEqualTo(1);
        assertThat(actualDtoObject.getAdditionalParameters()).isEqualTo(Collections.singletonMap("TEST_KEY", "TEST_VALUE"));
    }

    @Test
    public void givenValidAppInstancePostRequestDto_whenMappingToWorkloadInstancePostRequestDto_thenWorkloadInstancePostRequestDtoInstantiatedWithCorrectValues() {
        final AppInstancePostRequestDto actualAppInstancePostRequestDto = new AppInstancePostRequestDto().appId(1L)
                .additionalParameters(Collections.singletonMap("TEST_KEY", "TEST_VALUE"));
        final WorkloadInstancePostRequestDto actualWorkloadInstanceDto = objectUnderTest.map(actualAppInstancePostRequestDto,
                WorkloadInstancePostRequestDto.class);

        assertThat(actualWorkloadInstanceDto.getAdditionalParameters()).isInstanceOf(Map.class);
        assertThat(actualWorkloadInstanceDto.getAdditionalParameters()).hasFieldOrPropertyWithValue("TEST_KEY", "TEST_VALUE");
    }

    @Test
    public void givenValidAppInstancePostRequestDto_whenMappingToWorkloadInstancePutRequestDto_thenWorkloadInstancePutRequestDtoInstantiatedWithCorrectValues() {
        final AppInstancePostRequestDto actualAppInstancePostRequestDto = new AppInstancePostRequestDto().appId(1L)
                .additionalParameters(Collections.singletonMap("TEST_KEY", "TEST_VALUE"));
        final WorkloadInstancePutRequestDto actualWorkloadInstancePutRequestDto = objectUnderTest.map(actualAppInstancePostRequestDto,
                WorkloadInstancePutRequestDto.class);

        assertThat(actualWorkloadInstancePutRequestDto.getAdditionalParameters()).isInstanceOf(Map.class);
        assertThat(actualWorkloadInstancePutRequestDto.getAdditionalParameters()).hasFieldOrPropertyWithValue("TEST_KEY", "TEST_VALUE");
    }

    @Test
    public void givenValidWorkloadInstancePostRequestDto_whenMappingToWorkloadInstance_thenWorkloadInstanceInstantiatedWithCorrectValues() {
        final WorkloadInstancePostRequestDto actualWorkloadInstancePostRequestDto = new WorkloadInstancePostRequestDto()
                .workloadInstanceName("workloadInstanceName").cluster("localhost").namespace("appmanager-rapp").crdNamespace("namespace")
                .additionalParameters(Collections.singletonMap("TEST_KEY", "TEST_VALUE"));
        when(environmentHolder.getNamespaceEnv()).thenReturn("appmanager-rapp");
        final WorkloadInstance actualWorkloadInstance = objectUnderTest.map(actualWorkloadInstancePostRequestDto, WorkloadInstance.class);

        assertThat(actualWorkloadInstance.getWorkloadInstanceName()).isEqualTo("workloadInstanceName");
        assertThat(actualWorkloadInstance.getNamespace()).isEqualTo("appmanager-rapp");
        assertThat(actualWorkloadInstance.getCrdNamespace()).isEqualTo("namespace");
        assertThat(actualWorkloadInstance.getCluster()).isEqualTo("localhost");
        assertThat(actualWorkloadInstance.getAdditionalParameters()).isEqualTo(Collections.singletonMap("TEST_KEY", "TEST_VALUE"));
    }

    @Test
    public void givenValidWorkloadInstancePutRequestDto_whenMappingToWorkloadInstance_thenWorkloadInstanceInstantiatedWithCorrectValues() {
        final WorkloadInstance actualWorkloadInstance = objectUnderTest.map(
                new WorkloadInstancePutRequestDto().additionalParameters(Collections.singletonMap("TEST_KEY", "TEST_VALUE")), WorkloadInstance.class);

        assertThat(actualWorkloadInstance.getAdditionalParameters()).isEqualTo(Collections.singletonMap("TEST_KEY", "TEST_VALUE"));
    }
}
