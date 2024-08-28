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

package com.ericsson.oss.ae.utils.ssl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.security.KeyStore;

@Slf4j
class KeystoreHelperTest {

    @Mock
    private KeyStore keystore;

    @InjectMocks
    KeystoreHelper keystoreHelper;

    @Test
    void setNewTrustoreTest() {
        keystoreHelper.setNewTruststore();
    }

}
