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

package com.ericsson.oss.ae.utils.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import com.ericsson.oss.ae.clients.keycloak.dto.TokenDto;
import org.junit.jupiter.api.Test;

import com.ericsson.oss.ae.presentation.exceptions.InvalidInputException;

class JsonUtilsTest {

    @Test
    void givenValidMap_whenParsingToJson_thenReturnJsonString() {
        final String expectedJsonString = "{\"namespace\":\"namespace\"}";

        final Map<String, Object> testMap = Map.of("namespace", "namespace");
        final String actualString = JsonUtils.parseMapToJsonString(testMap);

        assertThat(actualString).isEqualTo(expectedJsonString);
    }

    @Test
    void givenInvalidMap_whenParsingToJson_thenReturnInvalidInputException() {
        final String expectedErrorMessage = "Error parsing JSON";
        final Map<String, Object> testMap = Map.of("", new Object());

        final InvalidInputException actualException = assertThrows(InvalidInputException.class, () -> {
            JsonUtils.parseMapToJsonString(testMap);
        });

        assertThat(actualException).hasMessage(expectedErrorMessage);
    }

    @Test
    void givenValidFile_whenParsingToJson_thenReturnInvalidInputException() {
        final String fileName = "expectedresponses/keycloak/TokenDto.json";
        final String expectedJsonString = "Bearer";
        final TokenDto actualObject = (TokenDto) JsonUtils.getObjectFromJsonFile(fileName,TokenDto.class);

        assertThat(actualObject.getTokenType()).isEqualTo(expectedJsonString);
    }
    @Test
    void givenInvalidFile_whenParsingToJson_thenReturnInvalidInputException() {
        final String fileName = "expectedresponses/keycloak/T.json";
        final String expectedErrorMessage = "Error parsing JSON";
        final IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () -> {
            JsonUtils.getObjectFromJsonFile(fileName,TokenDto.class);
        });
    }

}