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

import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

/**
 * Model class used for representing a response from App Onboarding's '/apps' endpoint.
 * <p>
 * App {@link App} is mapped to {@link AppDto} class.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class App {
    private Long id;
    private String name;
    private String username;
    private String version;
    private String size;
    private String vendor;
    private String type;
    private String onboardedDate;
    private String status;
    private String mode;
    private List<Artifact> artifacts;
    private String descriptorInfo;
    private List events;
}
