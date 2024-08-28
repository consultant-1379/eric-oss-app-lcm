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

package com.ericsson.oss.ae.utils.ssl;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class KeystoreHelper {

    private static final String CERT_TYPE = "X.509";

    private static final String DEFAULT_P = "changeit";

    private static final String KEYSTORE = "custom.keystore";

    private static final String MOUNT_PATH = "/mnt/certs/iam/ca.crt";

    private static final String DEFAULT_CACERT = "/var/lib/ca-certificates/java-cacerts";

    private KeystoreHelper() {
    }

    /**
     * Create a new truststore and set it as the default.
     */
    public static void setNewTruststore() {
        final String[] certs = {MOUNT_PATH};
        final String trustStorePath = System.getProperty("java.io.tmpdir") + File.separator + KEYSTORE;
        log.debug("New truststore path: " + trustStorePath);
        final char[] pss = DEFAULT_P.toCharArray();
        try {
            KeyStore keystore = loadDefaultTruststore();

            if (keystore == null) {
                keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(null, null);
            }

            int i = 0;
            for (String certFile : certs) {
                try (FileInputStream fileInputStream = new FileInputStream(certFile);
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)
                ) {
                    while (bufferedInputStream.available() > 0) {
                        try {
                            Certificate certificate = CertificateFactory.getInstance(CERT_TYPE).generateCertificate(bufferedInputStream);
                            keystore.setCertificateEntry("customAlias"+i, certificate);
                        } catch (CertificateException ex) {
                            log.error("Cannot add certificate as it generated an empty input, skip it, iteration, {}", i);
                        }
                        i++;
                    }
                }
            }
            keystore.store(new FileOutputStream(trustStorePath), pss);
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", new String(pss));
            System.setProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException ex) {
            log.error("Tried to set new truststore at startup, all subsequent calls towards IAM mare result in failure if no correct CA is setup");
            log.error("Exception message: {}", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Read default cacert certificates
     * @return keystore located at jvm cacerts, if not found null will be returned
     */
    private static KeyStore loadDefaultTruststore() {
        KeyStore keystore = null;
        try {
            String javaHome = System.getProperty("java.home");
            String filename = "".equals(javaHome) || javaHome == null ? DEFAULT_CACERT.replace("/", File.separator) : (javaHome + "/lib/security/cacerts").replace("/", File.separator);
            try (FileInputStream is = new FileInputStream(filename)) {
                keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(is, DEFAULT_P.toCharArray());
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException ex) {
            log.error("Error reading default truststore certificates");
            log.error("Exception message: {}", ex.getMessage());
        }
        return keystore;
    }
}
