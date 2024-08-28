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

package com.ericsson.oss.ae.utils.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;

import com.ericsson.oss.ae.clients.helmorchestrator.dto.InstantiateWorkloadDto;
import com.ericsson.oss.ae.clients.helmorchestrator.dto.TerminateWorkloadDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceOperationPostRequestDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePostRequestDto;

class MapperUtilsTest {
    @Test
    void givenValidInstantiateWorkloadDto_whenMappingToMultiValueMap_thenDtoShouldBeMappedWithCorrectTypeAndMediaType() {
        final InstantiateWorkloadDto testObject = createTestDto();
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);

        assertThat(actualObject).hasFieldOrProperty("helmSource");
        assertThat(actualObject).hasFieldOrProperty("values");
        assertThat(actualObject).hasFieldOrProperty("clusterConnectionInfo");
        assertThat(actualObject).hasFieldOrProperty("workloadInstancePostRequestDto");

        assertThat(actualObject.get("helmSource").get(0)).isInstanceOf(HttpEntity.class);
        assertThat(actualObject.get("values").get(0)).isInstanceOf(HttpEntity.class);
        assertThat(actualObject.get("clusterConnectionInfo").get(0)).isInstanceOf(HttpEntity.class);
        assertThat(actualObject.get("workloadInstancePostRequestDto").get(0)).isInstanceOf(HttpEntity.class);
    }

    @Test
    void givenValidTerminateWorkloadDto_whenMappingToMultiValueMap_thenDtoShouldBeMappedWithCorrectType() {
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(
                TerminateWorkloadDto.builder().workloadInstanceOperationPostRequestDto(new WorkloadInstanceOperationPostRequestDto()).build());

        assertThat(actualObject).hasFieldOrProperty("workloadInstanceOperationPostRequestDto");
        assertThat(actualObject.get("workloadInstanceOperationPostRequestDto").get(0)).isInstanceOf(HttpEntity.class);
    }

    private InstantiateWorkloadDto createTestDto() {
        final ByteArrayResource helmSources = new ByteArrayResource(new byte[] { 1, 2, 3, 4, 5 });
        final ByteArrayResource values = new ByteArrayResource(new byte[] { 2, 3, 4, 5, 6 });
        final ByteArrayResource clusterConnectInfo = new ByteArrayResource(new byte[] { 3, 4, 5, 6, 7 });
        return InstantiateWorkloadDto.builder().workloadInstancePostRequestDto(new WorkloadInstancePostRequestDto()).helmSource(helmSources)
                .values(values).clusterConnectionInfo(clusterConnectInfo).build();
    }
}
