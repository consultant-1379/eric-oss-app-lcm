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

package com.ericsson.oss.ae.acm.utils;

import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for parsing json file for Keycloak.
 */
@Slf4j
public final class JsonParser {

    /**
     * Private constructor
     *
     */
    private JsonParser() {
    }

    /**
     * Get data from json file
     *
     * @param filename file to get values.
     * @param sampleClass Response class structure.
     * @return Object.
     */
    public static Object getObjectFromJsonFile(final String filename, final Class<?> sampleClass) {
        log.info("Get Object From Json File");
        final Gson gson = new Gson();
        final InputStream inputStream = getClasspathResourceAsStream(filename);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return gson.fromJson(new JsonReader(inputStreamReader), sampleClass);
    }
}
