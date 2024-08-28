/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.ae.acm.common.constant.AppLcmError;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


/**
 * Class for object mapper operations
 */
@Component
@Slf4j
public class MapperUtil {

    /**
     * Convert String to List of Map
     *
     * @param property - String property
     */
    public List<Map<String, Object>> parsePropertyObjectToHashMapList(final String property) {
        try {
            return new ObjectMapper().readValue(property, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (JsonProcessingException ex) {
            log.error("Exception while converting String to List<Map> for property: {}", property, ex);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.DEFAULT_APP_LCM_ERROR);
        }
    }

    /**
     * Convert String to Map
     *
     * @param property - String property
     */
    public Map<String, Object> parsePropertyObjectToHashMap(final String property) {
        try {
            return new ObjectMapper().readValue(property, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException ex) {
            log.error("Exception while converting string to Map for property: {}", property, ex);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.DEFAULT_APP_LCM_ERROR);
        }
    }

    /**
     * Convert object to String
     *
     * @param propertyObject - Object property
     */
    public String parseObjectToString(final Object propertyObject) {
        try {
            return new ObjectMapper().writeValueAsString(propertyObject);
        } catch (JsonProcessingException ex) {
            log.error("Exception while converting object to string", ex);
            throw new AppLcmException(HttpStatus.INTERNAL_SERVER_ERROR, AppLcmError.DEFAULT_APP_LCM_ERROR);
        }
    }

}
