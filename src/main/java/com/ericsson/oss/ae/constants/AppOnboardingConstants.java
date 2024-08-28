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

package com.ericsson.oss.ae.constants;

import static com.ericsson.oss.ae.constants.AppLcmConstants.APPS;
import static com.ericsson.oss.ae.constants.AppLcmConstants.SLASH;

/**
 * Constants class for App Onboarding.
 */
public final class AppOnboardingConstants {
    public static final String APP_ONBOARDING_CURRENT_VERSION = "v1";
    public static final String APP_ONBOARDING_ARTIFACT_FILE = "file";
    public static final String APP_ONBOARDING_APPS_URL = SLASH + APP_ONBOARDING_CURRENT_VERSION + SLASH + APPS;
    public static final String APP_ONBOARDING_ARTIFACTS = "artifacts";
    public static final String APP_ONBOARDING_MODE_DISABLED = "DISABLED";
    public static final String APP_ONBOARDING_METHOD_INSTANTIATION = "instantiation";
    public static final String APP_ONBOARDING_METHOD_DELETION = "deletion";
    public static final String APP_ONBOARDING_METHOD_UPDATE = "update";
    public static final String APP_ONBOARDING_DISABLE = "disable";
    public static final String APP_ONBOARDING_ENABLE = "enable";
    public static final String APP_ONBOARDING_REQUESTED_APP = "Requested app ";
    public static final String APP_ONBOARDING_IS_NOT = " is not " ;
    public static final String APP_ONBOARDING_CONTACT_ADMIN = "Please contact app admin to ";
    public static final String APP_ONBOARDING_THE_APP = " the app for app ";
    public static final String APP_ONBOARDING_UPDATE_STATUS_ERROR = "Error updating status of deleting app ";
    public static final String APP_ONBOARDING_DELETING_ERROR = "Error deleting app ";

    private AppOnboardingConstants() {
    }
}
