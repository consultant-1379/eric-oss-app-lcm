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

package com.ericsson.oss.ae.acm.utils.file;

import org.springframework.core.io.ByteArrayResource;

import lombok.EqualsAndHashCode;

/**
 * This class extends ByteArrayResource. This class allows a file name to be set for a byte array.
 */
@EqualsAndHashCode
public class FileNameAwareByteArrayResource extends ByteArrayResource {
    private final String fileName;

    /**
     * Constructor for creating a FileNameAwareByteArrayResource.
     *
     * @param fileName
     *            String containing the name of the file.
     * @param byteArray
     *            Byte Array containing the contents of a file.
     * @param description
     *            String containing the description of a file.
     *
     */
    public FileNameAwareByteArrayResource(final String fileName, final byte[] byteArray, final String description) {
        super(byteArray, description);
        this.fileName = fileName;
    }

    public FileNameAwareByteArrayResource(final String fileName, final byte[] byteArray) {
        super(byteArray);
        this.fileName = fileName;
    }

    @Override
    public String getFilename() {
        return fileName;
    }
}
