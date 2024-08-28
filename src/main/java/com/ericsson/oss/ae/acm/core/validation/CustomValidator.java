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

package com.ericsson.oss.ae.acm.core.validation;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.VALIDATION_UUID_ERROR;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.common.exception.AppLcmException;

@Slf4j
public class CustomValidator {

    private CustomValidator () {}

    public static UUID validateUUID(String id) {
        Pattern uuidRegex =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        if (id != null && uuidRegex.matcher(id).matches()) {
            return UUID.fromString(id);
        }
        throw new AppLcmException(HttpStatus.BAD_REQUEST, VALIDATION_UUID_ERROR);
    }
}
