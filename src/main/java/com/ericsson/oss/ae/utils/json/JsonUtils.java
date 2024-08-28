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

package com.ericsson.oss.ae.utils.json;

import static com.ericsson.oss.ae.utils.file.loader.ResourceLoaderUtils.getClasspathResourceAsStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.InvalidInputException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonUtils {

    private JsonUtils() {
    }

    public static String parseMapToJsonString(final Map<String, Object> source) {
        log.info("Parse Map To Json String");
        try {
            return new ObjectMapper().writeValueAsString(source);
        } catch (final JsonProcessingException e) {
            log.error("Error parsing JSON due to {}", e.getMessage());
            throw new InvalidInputException(AppLcmError.INVALID_INPUT_EXCEPTION, "Error parsing JSON", e);
        }
    }

    public static Object getObjectFromJsonFile(String filename,final Class<?> sampleClass) {
        log.info("Get Object From Json File");
        Gson gson = new Gson();
        InputStream inputStream = getClasspathResourceAsStream(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return  gson.fromJson(new JsonReader(inputStreamReader), sampleClass);
    }
}
