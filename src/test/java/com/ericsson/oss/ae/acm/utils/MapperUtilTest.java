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

import static com.ericsson.oss.ae.acm.TestConstants.TIMEOUT_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.ae.acm.common.exception.AppLcmException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MapperUtilTest {

    private MapperUtil mapperUtilUnderTest;

    @BeforeEach
    public void setUp() {
        mapperUtilUnderTest = new MapperUtil();
    }

    @Test
    public void testParsePropertyObjectToHashMapList() throws JsonProcessingException {
        final Map<String,Object> propertyMap = new HashMap<>();
        propertyMap.put(TIMEOUT_KEY, 2000);
        final List<Map<String, Object>> result = mapperUtilUnderTest.parsePropertyObjectToHashMapList(new ObjectMapper().writeValueAsString(List.of(propertyMap)));
        assertThat(result).isNotNull();
    }

    @Test
    public void testParsePropertyObjectToHashMapListThrowsException() throws JsonProcessingException {
        final Map<String,Object> propertyMap = new HashMap<>();
        propertyMap.put(TIMEOUT_KEY, 2000);
        assertThatThrownBy(
                () -> mapperUtilUnderTest.parsePropertyObjectToHashMapList(new ObjectMapper().writeValueAsString(propertyMap))).isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testParsePropertyObjectToHashMap() throws JsonProcessingException {
        final Map<String,Object> propertyMap = new HashMap<>();
        propertyMap.put(TIMEOUT_KEY, 2000);
        final Map<String, Object> result = mapperUtilUnderTest.parsePropertyObjectToHashMap(new ObjectMapper().writeValueAsString(propertyMap));
        assertThat(result).isNotNull();
    }

    @Test
    public void testParsePropertyObjectToHashMapThrowsException() throws JsonProcessingException {
        final Map<String,Object> propertyMap = new HashMap<>();
        propertyMap.put(TIMEOUT_KEY, 2000);
        assertThatThrownBy(
                () -> mapperUtilUnderTest.parsePropertyObjectToHashMap("")).isInstanceOf(AppLcmException.class);
    }

    @Test
    public void testParseObjectToString() {
        assertThat(mapperUtilUnderTest.parseObjectToString(2000)).isEqualTo("2000");
    }

    @Test
    public void testParseObjectToStringThrowsException() {
        Object mockItem = mock(Object.class);
        assertThatThrownBy(
                () -> mapperUtilUnderTest.parseObjectToString(mockItem)).isInstanceOf(AppLcmException.class);
    }

}
