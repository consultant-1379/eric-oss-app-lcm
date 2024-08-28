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

package com.ericsson.oss.ae.clients.helmorchestrator;

import com.ericsson.oss.ae.clients.helmorchestrator.dto.InstantiateWorkloadDto;
import com.ericsson.oss.ae.clients.helmorchestrator.dto.TerminateWorkloadDto;
import com.ericsson.oss.ae.clients.helmorchestrator.dto.UpdateWorkloadDto;
import com.ericsson.oss.ae.model.entity.CredentialEvent;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.HelmOrchestratorException;
import com.ericsson.oss.ae.utils.rest.RequestHandler;
import com.ericsson.oss.management.lcm.api.model.OperationDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstanceOperationPostRequestDto;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePostRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class HelmOrchestratorClientImplTest {
    @Autowired
    private HelmOrchestratorClientImpl objectUnderTest;

    @MockBean
    private RequestHandler requestHandler;

    @Test
    public void givenAValidTerminateWorkloadDto_whenABadRequestStatusIsReturnedFromOrchestratorForTerminationRequestForAppInstance_thenExceptionCaughtExpectedErrorResponseReturned() {
        when(requestHandler.sendRestRequestUsingParticipant(any(), any(),any(), any())).thenThrow(new RestClientException("test"));

        final HelmOrchestratorException actualException = assertThrows(HelmOrchestratorException.class, () -> {
            objectUnderTest.terminateAppInstanceById("workloadInstanceId", TerminateWorkloadDto.builder()
                    .workloadInstanceOperationPostRequestDto(new WorkloadInstanceOperationPostRequestDto().type("terminate")).build());
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.HELM_ORCHESTRATOR_TERMINATION_ERROR);
        assertThat(actualException.getMessage()).isEqualTo("Error terminating app instance with for Workload Instance with ID workloadInstanceId");
    }

    @Test
    public void givenValidTerminateWorkloadDto_whenAnAcceptedStatusIsReturnedFromOrchestratorForTerminationRequestForAppInstance_thenHelmOrchestratorReturnsCorrectResponse() {
        when(requestHandler.sendRestRequestUsingParticipant(any(), any(), any(),any())).thenReturn(new ResponseEntity(HttpStatus.ACCEPTED));

        final ResponseEntity actualTerminationResponse = objectUnderTest.terminateAppInstanceById("workloadInstanceId", TerminateWorkloadDto.builder()
                .workloadInstanceOperationPostRequestDto(new WorkloadInstanceOperationPostRequestDto().type("terminate")).build());

        assertThat(actualTerminationResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void givenValidAppInstanceId_whenAnOkStatusIsReturnedFromOrchestratorForDeletionRequestForAppInstance_thenHelmOrchestratorReturnsCorrectHttpStatus() {
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any())).thenReturn(new ResponseEntity(HttpStatus.OK));

        final ResponseEntity actualDeletionResponse = objectUnderTest.deleteWorkloadInstanceId("1");

        assertThat(actualDeletionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenAValidOperationId_whenAnOkStatusIsReturnedFromOrchestratorForGetRequestForOperation_thenResponseEntityReturnedWithCorrectValues() {
        final ResponseEntity expectedOperationResponse = new ResponseEntity(
                new OperationDto().operationId("operationId").type("type").workloadInstanceId("workloadInstanceId"), HttpStatus.OK);

        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any())).thenReturn(expectedOperationResponse);

        final ResponseEntity<OperationDto> actualOperationResponse = objectUnderTest.getOperation("operationId");

        assertThat(actualOperationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualOperationResponse.getBody().getOperationId()).isEqualTo("operationId");
        assertThat(actualOperationResponse.getBody().getWorkloadInstanceId()).isEqualTo("workloadInstanceId");
        assertThat(actualOperationResponse.getBody().getType()).isEqualTo("type");
    }

    @Test
    public void givenAValidOperationId_whenABadRequestStatusIsReturnedFromOrchestratorForGetRequestForOperation_thenExceptionCaughtExpectedErrorResponseReturned() {
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any())).thenThrow(new RestClientException("test"));

        final HelmOrchestratorException actualException = assertThrows(HelmOrchestratorException.class, () -> {
            objectUnderTest.getOperation("operationId");
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.HELM_ORCHESTRATOR_OPERATION_ERROR);
        assertThat(actualException.getMessage()).isEqualTo("Error retrieving operation for operation ID operationId");
    }

    @Test
    public void givenAValidOperationId_whenAnOkStatusIsReturnedFromOrchestratorForGetRequestForOperationLogs_thenResponseEntityReturnedWithCorrectValues() {
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any())).thenReturn(new ResponseEntity<>("TestLogs", HttpStatus.OK));

        final ResponseEntity<String> operationLogsResponse = objectUnderTest.getOperationLogs("operationId");

        assertThat(operationLogsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(operationLogsResponse.getBody()).isEqualTo("TestLogs");
    }

    @Test
    public void givenAValidOperationId_whenABadRequestStatusIsReturnedFromGetRequestForOperationLogs_thenExceptionCaughtExpectedErrorResponseReturned() {
        when(requestHandler.createAndSendRestRequest(any(), any(), any(), any())).thenThrow(new RestClientException("test"));

        final HelmOrchestratorException actualException = assertThrows(HelmOrchestratorException.class, () -> {
            objectUnderTest.getOperationLogs("operationId");
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.HELM_ORCHESTRATOR_OPERATION_LOGS_ERROR);
        assertThat(actualException.getMessage()).isEqualTo("Error retrieving operation logs for operation ID operationId");
    }

    @Test
    public void givenAValidInstantiateWorkloadDto_whenAnOkStatusIsReturnedFromPostRequestForInstantiateApp_thenReturnWorkloadInstanceDtoWithCorrectValues() {
        when(requestHandler.sendRestRequestUsingParticipant(any(), any(), any(), any())).thenReturn(new ResponseEntity<>(
                new WorkloadInstanceDto().workloadInstanceId("workloadInstanceId").additionalParameters(Collections.singletonMap("TEST", "MAP")),
                HttpStatus.OK));

        final ResponseEntity<WorkloadInstanceDto> actualInstantiateResponseEntity = objectUnderTest
                .instantiateApp(InstantiateWorkloadDto.builder().build());

        assertThat(actualInstantiateResponseEntity.getBody().getAdditionalParameters()).isEqualTo(Collections.singletonMap("TEST", "MAP"));
        assertThat(actualInstantiateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualInstantiateResponseEntity.getBody().getWorkloadInstanceId()).isEqualTo("workloadInstanceId");
    }

    @Test
    public void givenAValidInstantiateWorkloadDto_whenABadRequestStatusIsReturnedFromPostRequestForInstantiateApp_thenExceptionCaughtExpectedErrorResponseReturned() {
        when(requestHandler.sendRestRequestUsingParticipant(any(), any(), any(), any())).thenThrow(new RestClientException("test"));
        WorkloadInstancePostRequestDto workloadInstancePostRequestDto = new WorkloadInstancePostRequestDto();
        final HelmOrchestratorException actualException = assertThrows(HelmOrchestratorException.class, () -> {
            objectUnderTest.instantiateApp(InstantiateWorkloadDto.builder().workloadInstancePostRequestDto(workloadInstancePostRequestDto).build());
        });
        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.HELM_ORCHESTRATOR_FAILURE_TO_INSTANTIATE_APP);
        assertThat(actualException.getMessage())
                .isEqualTo("Error instantiating application with Request Entity: " + InstantiateWorkloadDto.builder().workloadInstancePostRequestDto(workloadInstancePostRequestDto).build());
    }

    @Test
    public void givenAValidUpdateWorkloadDto_WhenAcceptedResponseIsReturnedFromUpdateRequestForAppInstance_ThenReturnWorkloadInstanceDtoWithCorrectValues() {
        when(requestHandler.sendRestRequestUsingParticipant(any(), any(), any(),any())).thenReturn(new ResponseEntity<>(
                new WorkloadInstanceDto().workloadInstanceId("workloadInstanceId").additionalParameters(Collections.singletonMap("TEST", "MAP")),
                HttpStatus.ACCEPTED));

        final ResponseEntity<WorkloadInstanceDto> actualInstantiateResponseEntity = objectUnderTest.updateApp(UpdateWorkloadDto.builder().build(),
                "workloadInstanceId",1L);

        assertThat(actualInstantiateResponseEntity.getBody().getAdditionalParameters()).isEqualTo(Collections.singletonMap("TEST", "MAP"));
        assertThat(actualInstantiateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(actualInstantiateResponseEntity.getBody().getWorkloadInstanceId()).isEqualTo("workloadInstanceId");
    }

    @Test
    public void givenAValidUpdateWorkloadDto_whenABadRequestStatusIsReturnedFromPutRequestForUpdateApp_thenExceptionCaughtExpectedErrorResponseReturned() {
        when(requestHandler.sendRestRequestUsingParticipant(any(), any(), any(),any())).thenThrow(new RestClientException("test"));

        final HelmOrchestratorException actualException = assertThrows(HelmOrchestratorException.class, () -> {
            objectUnderTest.updateApp(UpdateWorkloadDto.builder().build(), "workloadInstanceId", 1L);
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(AppLcmError.HELM_ORCHESTRATOR_FAILURE_TO_UPDATE_APP);
        assertThat(actualException.getMessage()).isEqualTo("Error updating application with Request Entity: " + UpdateWorkloadDto.builder().build());
    }

}
