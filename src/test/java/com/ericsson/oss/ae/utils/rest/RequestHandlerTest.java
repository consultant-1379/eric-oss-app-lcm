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

package com.ericsson.oss.ae.utils.rest;

import com.ericsson.oss.ae.clients.apponboarding.AppOnboardingClientImpl;
import com.ericsson.oss.ae.clients.helmorchestrator.dto.InstantiateWorkloadDto;
import com.ericsson.oss.ae.model.participant.RestRequest;
import com.ericsson.oss.ae.participant.RestService;
import com.ericsson.oss.ae.participant.invoker.ParticipantCommandInvoker;
import com.ericsson.oss.ae.presentation.exceptions.AppOnboardingAppNotExistException;
import com.ericsson.oss.ae.utils.mapper.MapperUtils;
import com.ericsson.oss.management.lcm.api.model.WorkloadInstancePostRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;

import static com.ericsson.oss.ae.constants.AppOnboardingConstants.APP_ONBOARDING_UPDATE_STATUS_ERROR;
import static com.ericsson.oss.ae.presentation.exceptions.AppLcmError.APP_ONBOARDING_APP_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class RequestHandlerTest {
    @Autowired
    private AppOnboardingClientImpl appOnBoardingClient;

    @Autowired
    private RequestHandler objectUnderTest;

    @MockBean
    private RestService restService;

    @MockBean
    private ParticipantCommandInvoker participantCommandInvoker;



    @Test
    void givenValidDto_whenRequestingCall_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        when(restService.callRestEndpoint(any())).thenReturn(new ResponseEntity<>(actualObject,HttpStatus.ACCEPTED));
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.createAndSendRestRequest("url",
                                                                                                                ArrayList.class, MediaType.APPLICATION_JSON,HttpMethod.POST);
        assertThat(actualInstantiateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    }

    @Test
    void givenValidDtoAndToken_whenRequestingCall_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        String[] token = new String[1];
        token[0] = "ey1234";
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        when(restService.callRestEndpoint(any())).thenReturn(new ResponseEntity<>(actualObject,HttpStatus.ACCEPTED));
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.createAndSendRestRequest("url",
                                                                                                                ArrayList.class, MediaType.APPLICATION_JSON,HttpMethod.POST,token);
        assertThat(actualInstantiateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    }

    @Test
    void givenValidDtoAndTokenContainsNull_whenRequestingCall_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        String[] token = new String[1];
        token[0] = null;
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        when(restService.callRestEndpoint(any())).thenReturn(new ResponseEntity<>(actualObject,HttpStatus.ACCEPTED));
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.createAndSendRestRequest("url",
                                                                                                                ArrayList.class, MediaType.APPLICATION_JSON,HttpMethod.POST,token);
        assertThat(actualInstantiateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    }
    @Test
    void givenValidDtoAndTokenIsNull_whenRequestingCall_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        when(restService.callRestEndpoint(any())).thenReturn(new ResponseEntity<>(actualObject,HttpStatus.ACCEPTED));
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.createAndSendRestRequest("url",
                                                                                                                ArrayList.class, MediaType.APPLICATION_JSON,HttpMethod.POST,null);
        assertThat(actualInstantiateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    }

    @Test
    void givenValidDtoAndToken_whenRequestingCallKeyCloak_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        String[] token = new String[1];
        token[0] = "ey1234";
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        when(participantCommandInvoker.executeHelmCommand()).thenReturn(new ResponseEntity<>(actualObject,HttpStatus.ACCEPTED));
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.sendRestRequestToKeycloak(actualObject,"url",
                                                                                                                 ArrayList.class, MediaType.APPLICATION_JSON,HttpMethod.POST,token);
        assertThat(actualInstantiateResponseEntity).isEqualTo(null);

    }
    @Test
    void givenValidDto_whenRequestingCallKeyCloak_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        RestRequest restRequest = new RestRequest();
        restRequest.setUrl("url");
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.sendRestRequestToKeycloak(actualObject,"url",
                                                                                                                 ArrayList.class, MediaType.APPLICATION_JSON,HttpMethod.POST);
        assertThat(actualInstantiateResponseEntity).isEqualTo(null);
    }

    @Test
    void givenValidDtoAndToken_whenRequestingCallUsingParticipant_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        when(participantCommandInvoker.executeHelmCommand()).thenReturn(new ResponseEntity<>(actualObject,HttpStatus.ACCEPTED));
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.sendRestRequestUsingParticipant(actualObject,"url",
                                                                                                                       ArrayList.class,HttpMethod.POST);
        assertThat(actualInstantiateResponseEntity).isEqualTo(null);
    }
    @Test
    void givenValidDtoAndTokenEmpty_whenRequestingCallKeyCloak_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        String[] token = new String[1];
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        when(participantCommandInvoker.executeHelmCommand()).thenReturn(new ResponseEntity<>(actualObject,HttpStatus.ACCEPTED));
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.sendRestRequestToKeycloak(actualObject,"url",
                                                                                                                 ArrayList.class, MediaType.APPLICATION_JSON,HttpMethod.POST,token);
        assertThat(actualInstantiateResponseEntity).isEqualTo(null);

    }
    @Test
    void givenValidDtoAndTokenContainsNull_whenRequestingCallKeyCloak_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        String[] token = new String[1];
        token[0] = null;
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        when(participantCommandInvoker.executeHelmCommand()).thenReturn(new ResponseEntity<>(actualObject,HttpStatus.ACCEPTED));
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.sendRestRequestToKeycloak(actualObject,"url",
                                                                                                                 ArrayList.class, MediaType.APPLICATION_JSON,HttpMethod.POST,token);
        assertThat(actualInstantiateResponseEntity).isEqualTo(null);

    }
    @Test
    void givenValidDtoAndTokenIsNull_whenRequestingCallKeyCloak_thenReturnCorrectResponse() {
        final InstantiateWorkloadDto testObject = createTestDto();
        final MultiValueMap<String, Object> actualObject = MapperUtils.mapRequestModelToMultiValueMap(testObject);
        when(participantCommandInvoker.executeHelmCommand()).thenReturn(new ResponseEntity<>(actualObject,HttpStatus.ACCEPTED));
        final ResponseEntity<Object> actualInstantiateResponseEntity = objectUnderTest.sendRestRequestToKeycloak(actualObject,"url",
                                                                                                                 ArrayList.class, MediaType.APPLICATION_JSON,HttpMethod.POST,null);
        assertThat(actualInstantiateResponseEntity).isEqualTo(null);

    }


    private InstantiateWorkloadDto createTestDto() {
        final ByteArrayResource helmSources = new ByteArrayResource(new byte[] { 1, 2, 3, 4, 5 });
        final ByteArrayResource values = new ByteArrayResource(new byte[] { 2, 3, 4, 5, 6 });
        final ByteArrayResource clusterConnectInfo = new ByteArrayResource(new byte[] { 3, 4, 5, 6, 7 });
        return InstantiateWorkloadDto.builder().workloadInstancePostRequestDto(new WorkloadInstancePostRequestDto()).helmSource(helmSources)
            .values(values).clusterConnectionInfo(clusterConnectInfo).build();
    }

    @Test
    void givenOkResponse_whenUpdateStatusForDeletion_thenReturnSuccessfulResponse() {

        when(restService.callRestEndpoint(any(RestRequest.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity updateStatusResponse = appOnBoardingClient.updateStatusForDeletion(anyLong());

        assertThat(updateStatusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenBadRequestResponse_whenUpdateStatusForDeletion_thenThrowAppOnBoardingAppNotExistException() {

        when(restService.callRestEndpoint(any(RestRequest.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        ResponseEntity updateStatusResponse = appOnBoardingClient.updateStatusForDeletion(anyLong());

        assertThat(updateStatusResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void givenRestClientException_whenUpdateStatusForDeletion_thenThrowAppOnBoardingAppNotExistException() {
        final Long appId = 1L;

        when(restService.callRestEndpoint(any(RestRequest.class)))
            .thenThrow(new RestClientException("test"));

        final AppOnboardingAppNotExistException actualException = assertThrows(AppOnboardingAppNotExistException.class, () -> {
            appOnBoardingClient.updateStatusForDeletion(appId);
        });

        assertThat(actualException.getAppLcmError()).isEqualTo(APP_ONBOARDING_APP_NOT_FOUND);
        assertThat(actualException.getMessage()).isEqualTo(APP_ONBOARDING_UPDATE_STATUS_ERROR + appId);
    }
}
