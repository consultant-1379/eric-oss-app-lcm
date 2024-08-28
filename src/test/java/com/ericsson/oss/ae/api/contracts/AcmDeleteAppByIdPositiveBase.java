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

package com.ericsson.oss.ae.api.contracts;

import static com.ericsson.oss.ae.acm.TestConstants.EEFD_22230;
import static org.mockito.Mockito.doNothing;

import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AcmDeleteAppByIdPositiveBase extends AcmAppsBase {
    @Override
    public void validate() {
        doNothing().when(service).deleteAppById(EEFD_22230);
    }
}
