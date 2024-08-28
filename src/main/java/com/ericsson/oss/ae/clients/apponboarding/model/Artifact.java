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

package com.ericsson.oss.ae.clients.apponboarding.model;

import com.ericsson.oss.ae.clients.apponboarding.dto.ArtifactDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class used for representing responses from App Onboarding's "/artifacts" endpoint.
 * <p>
 * * Artifact Dto {@link ArtifactDto} is mapped to this model class.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Artifact {

    private Long id;
    private String name;
    private String type;
    private String version;
    private String status;
    private String location;
}
