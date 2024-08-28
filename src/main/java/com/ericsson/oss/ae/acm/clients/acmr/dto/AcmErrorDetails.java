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
package com.ericsson.oss.ae.acm.clients.acmr.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Response returned when no extra output fields are needed.
 */
@Getter
@Setter
@ToString
public class AcmErrorDetails{

    /**
     * Optional detailed message in error cases.
     */
    private String errorDetails;
}
