/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.ae.model;

import com.ericsson.oss.ae.model.entity.HealthStatus;
import com.ericsson.oss.ae.model.entity.TargetStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Component
public class AppInstanceFilter {

    private List<Long> instanceIds;

    private List<Long> appOnBoardingAppId;

    private List<HealthStatus> healthStatus;

    private List<TargetStatus> targetStatus;

    private List<ZonedDateTime> createTimeStamp;

    private List<ZonedDateTime> updateTimeStamp;

    private List<String> additionalParameters;

}
