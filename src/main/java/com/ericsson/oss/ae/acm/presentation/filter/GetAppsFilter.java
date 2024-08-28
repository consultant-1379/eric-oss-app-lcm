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
package com.ericsson.oss.ae.acm.presentation.filter;

public record GetAppsFilter(String name, String version, String mode, String status, String type, String offset, String limit) {

    public static final class GetAppsFilterBuilder {

        String name;
        String version;
        String mode;
        String status;
        String type;
        String offset;
        String limit;

        public GetAppsFilterBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GetAppsFilterBuilder version(String version) {
            this.version = version;
            return this;
        }

        public GetAppsFilterBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public GetAppsFilterBuilder status(String status) {
            this.status = status;
            return this;
        }

        public GetAppsFilterBuilder type(String type) {
            this.type = type;
            return this;
        }

        public GetAppsFilterBuilder offset(String offset) {
            this.offset = offset;
            return this;
        }

        public GetAppsFilterBuilder limit(String limit) {
            this.limit = limit;
            return this;
        }

        public GetAppsFilter build() {
            return new GetAppsFilter(name, version, mode, status, type, offset, limit);
        }
    }
}
