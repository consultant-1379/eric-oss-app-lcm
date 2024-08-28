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

package com.ericsson.oss.ae.presentation.exceptions;

import static com.ericsson.oss.ae.constants.AppOnboardingConstants.*;

/**
 * AppOnBoardingModeException for ENABLED or DISABLED mode
 */
public class AppOnBoardingModeException extends AppLcmException {

    public AppOnBoardingModeException(final AppLcmError appLcmError, final Long appId, final String method, final String appInstancesUrl) {
        super(appLcmError, APP_ONBOARDING_REQUESTED_APP + appId + APP_ONBOARDING_IS_NOT +"enabled. "
                  + APP_ONBOARDING_CONTACT_ADMIN + APP_ONBOARDING_ENABLE + APP_ONBOARDING_THE_APP + method,
                appInstancesUrl);
    }

    public AppOnBoardingModeException(AppLcmError appLcmError, Long appId, String modeDisabled, String method, String appInstancesUrl) {
        super(appLcmError, APP_ONBOARDING_REQUESTED_APP + appId + APP_ONBOARDING_IS_NOT + modeDisabled + ". " +
                  APP_ONBOARDING_CONTACT_ADMIN + APP_ONBOARDING_DISABLE + APP_ONBOARDING_THE_APP +method,
                appInstancesUrl);
    }
}