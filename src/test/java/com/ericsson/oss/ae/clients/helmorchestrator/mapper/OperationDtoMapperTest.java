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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ericsson.oss.ae.clients.helmorchestrator.model.Operation;
import com.ericsson.oss.management.lcm.api.model.OperationDto;

@SpringBootTest(classes = { OperationDtoMapper.class })
public class OperationDtoMapperTest {
    private static final String OPERATION_ID = "b5e276a9-aa96-491c-86d0-542d78d86d9e";
    private static final String WORKLOAD_INSTANCE_ID = "0b969e06-93a0-4780-8676-9be55cb29211";
    private static final String STATE = "COMPLETED";
    private static final String TYPE = "INSTANTIATE";
    private static final LocalDateTime START_TIME = LocalDateTime.of(LocalDate.of(2021, 11, 16), LocalTime.of(17, 31, 53, 183884));

    @Autowired
    OperationDtoMapper objectUnderTest;

    @Test
    public void givenValidOperationObject_WhenMappingOperationToOperationDto_ThenOperationDtoContainsCorrectValues() {
        final OperationDto actualOperationDto = objectUnderTest.map(Operation.builder().operationId(OPERATION_ID)
                .workloadInstanceId(WORKLOAD_INSTANCE_ID).state(STATE).type(TYPE).startTime(START_TIME).build(), OperationDto.class);

        assertThat(actualOperationDto.getOperationId()).isEqualTo("b5e276a9-aa96-491c-86d0-542d78d86d9e");
        assertThat(actualOperationDto.getWorkloadInstanceId()).isEqualTo("0b969e06-93a0-4780-8676-9be55cb29211");
        assertThat(actualOperationDto.getState()).isEqualTo("COMPLETED");
        assertThat(actualOperationDto.getType()).isEqualTo("INSTANTIATE");
        assertThat(actualOperationDto.getStartTime()).isEqualTo(START_TIME.toString());
    }

    @Test
    public void givenAValidOperationDtoObject_WhenMappingOperationDtoToOperation_ThenOperationContainsCorrectValues() {
        final Operation actualOperation = objectUnderTest.map(new OperationDto().operationId(OPERATION_ID).workloadInstanceId(WORKLOAD_INSTANCE_ID)
                .state(STATE).type(TYPE).startTime(START_TIME.toString()), Operation.class);

        assertThat(actualOperation.getOperationId()).isEqualTo("b5e276a9-aa96-491c-86d0-542d78d86d9e");
        assertThat(actualOperation.getWorkloadInstanceId()).isEqualTo("0b969e06-93a0-4780-8676-9be55cb29211");
        assertThat(actualOperation.getState()).isEqualTo("COMPLETED");
        assertThat(actualOperation.getType()).isEqualTo("INSTANTIATE");
        assertThat(actualOperation.getStartTime()).isEqualTo(START_TIME);
    }
}