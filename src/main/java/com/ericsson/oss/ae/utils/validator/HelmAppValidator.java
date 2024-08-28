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

package com.ericsson.oss.ae.utils.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.ae.clients.helmorchestrator.model.WorkloadInstance;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HelmAppValidator {
    /**
     * All namespace names must be valid RFC 1123 DNS labels. Template requirements: contain at most 63 characters contain only lowercase alphanumeric
     * characters or '-' start with an alphanumeric character end with an alphanumeric character
     */
    private static final Pattern NAMESPACE_PATTERN_EXPRESSION = Pattern.compile("^[a-z0-9][a-z0-9-]{0,61}[a-z0-9]$");

    private static final Pattern WORKLOAD_INSTANCE_NAME_PATTERN_EXPRESSION = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?");

    private HelmAppValidator() {
    }

    public static void validate(final WorkloadInstance workloadInstance) {

        final String name = workloadInstance.getWorkloadInstanceName();
        final String namespace = workloadInstance.getNamespace();
        log.info("Validate workload instance name: {} and namespace: {}", name, namespace);
        final StringBuilder errors = new StringBuilder();

        final boolean isValidName = matchByPattern(WORKLOAD_INSTANCE_NAME_PATTERN_EXPRESSION, name);
        if (!isValidName) {
            errors.append(String.format("WorkloadInstanceName %s is invalid. ", name)).append("\n");
        }

        final boolean isValidState = matchByPattern(NAMESPACE_PATTERN_EXPRESSION, namespace);
        if (!isValidState) {
            errors.append(String.format("Namespace %s is invalid.", namespace));
        }

        if (errors.length() != 0) {
            log.error("Invalid input: {}", errors);
            throw new InvalidInputException(AppLcmError.INVALID_INPUT_EXCEPTION, errors.toString());
        }
    }

    public static void isNamespaceValid(final String namespace) {
        log.info("Is namespace valid, namespace: {}", namespace);
        final boolean isValidState = matchByPattern(NAMESPACE_PATTERN_EXPRESSION, namespace);
        if (!isValidState) {
            log.error("Namespace; {} is invalid", namespace);
            throw new InvalidInputException(AppLcmError.INVALID_INPUT_EXCEPTION, "Namespace %s is invalid");
        }
    }

    private static boolean matchByPattern(final Pattern pattern, final String text) {
        log.trace("Match by pattern text: {}", text);
        final Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }
}
