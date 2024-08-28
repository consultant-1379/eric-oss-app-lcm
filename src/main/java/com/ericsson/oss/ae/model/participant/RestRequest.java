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

package com.ericsson.oss.ae.model.participant;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * This class is used to map model for Rest API call.
 */
@Component
public class RestRequest {

    private String url;
    private HttpMethod requestMethod;
    private HttpEntity request;
    private Class responseType;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(final HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public HttpEntity getRequest() {
        return request;
    }

    public void setRequest(final HttpEntity request) {
        this.request = request;
    }

    public Class getResponseType() {
        return responseType;
    }

    public void setResponseType(final Class responseType) {
        this.responseType = responseType;
    }
}
