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

package com.ericsson.oss.ae.acm.common.rest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Builds REST requests for ACM and Keycloak
 */

@Slf4j
@Component
public class RequestBuilder {

    /**
     * Generates request body to call the rest client end point.
     *
     * @param url               String Url used to build request.
     * @param requestBody       request body
     * @param responseType      Response class type
     * @param contentMediaType  Media type for the content
     * @param httpMethod        The Http method used for the request (GET or DELETE).
     * @param bearerToken       Bearer token for authorization
     * @param basicCredentials  Basic credentials for authorization
     * @return Returns get request response
     */
    public RestRequest createRequestContent(final String url, final String requestBody, final Class responseType, final MediaType contentMediaType,
                                            final HttpMethod httpMethod, final String bearerToken, final String basicCredentials) {
        log.info("Create Rest Request for response type: {}", responseType);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentMediaType);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (bearerToken != null) {
            headers.setBearerAuth(bearerToken);
        } else if (basicCredentials != null) {
            final byte[] authBytes = basicCredentials.getBytes(StandardCharsets.UTF_8);
            headers.setBasicAuth(Base64.getEncoder().encodeToString(authBytes));
        }
        HttpEntity<String> model = new HttpEntity<>(requestBody, headers);
        return new RestRequest(url, httpMethod, model, responseType);
    }

    public RestRequest createRequestContentWithNoBody(final String url, final Class responseType, final MediaType acceptedMediaType,
                                                      final HttpMethod httpMethod, final String bearerToken, final String basicCredentials) {
        log.debug("Create Rest Request for response type: {}", responseType);
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(acceptedMediaType));
        if (bearerToken != null) {
            headers.setBearerAuth(bearerToken);
        } else if (basicCredentials != null) {
            final byte[] authBytes = basicCredentials.getBytes(StandardCharsets.UTF_8);
            headers.setBasicAuth(Base64.getEncoder().encodeToString(authBytes));
        }
        final HttpEntity<String> model = new HttpEntity<>(headers);
        return new RestRequest(url, httpMethod, model, responseType);
    }

    public RestRequest createMultiValueRequestContent(final String url, final MultiValueMap<String, Object> requestBodyMap,
                                                      final Class responseType, final MediaType acceptedMediaType, final HttpMethod httpMethod) {
        log.info("Create Multi value rest request, http method: {}", httpMethod);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(acceptedMediaType));
        final HttpEntity<MultiValueMap<String, Object>> model = new HttpEntity<>(requestBodyMap, headers);
        return new RestRequest(url, httpMethod, model, responseType);
    }

}