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

package com.ericsson.oss.ae.utils;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.utils.file.FileNameAwareByteArrayResource;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileNameAwareByteArrayResourceTest {
    @Test
    public void givenValidByteArrayObjectParameters_WhenCreatingFileNameAwareByteArrayObject_ThenFileNameIsCorrect() {
        final ByteArrayResource itemUnderTest = new FileNameAwareByteArrayResource("TestFile", hexStringToByteArray("5465737446696c65"),
                "Test description");
        assertThat(itemUnderTest.getFilename()).isEqualTo("TestFile");
    }
}
