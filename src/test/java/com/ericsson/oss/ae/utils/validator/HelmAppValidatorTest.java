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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ericsson.oss.ae.clients.helmorchestrator.model.WorkloadInstance;
import com.ericsson.oss.ae.presentation.exceptions.InvalidInputException;

public class HelmAppValidatorTest {

    private static final String LONG_NAMESPACE = "111111111122222222223333333333444444444455555555556666666666-64c";
    private static final String UPPERCASE_NAMESPACE = "UPPERCASE";
    private static final String SYMBOL_NAMESPACE = "%";
    private static final String NAMESPACE_WITH_WRONG_FIRST_SYMBOL = "-name";
    private static final String NAMESPACE_WITH_WRONG_LAST_SYMBOL = "name-";

    @Test
    public void givenValidWorkloadInstance_whenValidating_thenShouldReturnNothing() {
        //Init
        final WorkloadInstance workloadInstance = getInstance();

        //Test method
        HelmAppValidator.validate(workloadInstance);
    }

    @Test
    public void givenTooLongWorkloadInstanceNamespace_whenValidating_thenThrowException() {
        //Init
        final WorkloadInstance workloadInstance = getInstance();
        workloadInstance.setNamespace(LONG_NAMESPACE);

        //Test method
        final Throwable thrown = catchThrowable(() -> {
            HelmAppValidator.validate(workloadInstance);
        });

        //Verify
        assertThat(thrown).isInstanceOf(InvalidInputException.class);
        assertThat(thrown.getMessage()).isNotBlank();
    }

    @Test
    public void givenUppercaseWorkloadInstanceNamespace_whenValidating_thenThrowException() {
        //Init
        final WorkloadInstance workloadInstance = getInstance();
        workloadInstance.setNamespace(UPPERCASE_NAMESPACE);

        //Test method
        final Throwable thrown = catchThrowable(() -> {
            HelmAppValidator.validate(workloadInstance);
        });

        //Verify
        assertThat(thrown).isInstanceOf(InvalidInputException.class);
        assertThat(thrown.getMessage()).isNotBlank();
    }

    @Test
    public void givenInvalidSymbolsInWorkloadInstanceNamespace_whenValidating_thenThrowException() {
        //Init
        final WorkloadInstance workloadInstance = getInstance();
        workloadInstance.setNamespace(SYMBOL_NAMESPACE);

        //Test method
        final Throwable thrown = catchThrowable(() -> {
            HelmAppValidator.validate(workloadInstance);
        });

        //Verify
        assertThat(thrown).isInstanceOf(InvalidInputException.class);
        assertThat(thrown.getMessage()).isNotBlank();
    }

    @Test
    public void givenWorkloadInstanceNamespaceStartedNonAlphanumeric_whenValidating_thenThrowException() {
        //Init
        final WorkloadInstance workloadInstance = getInstance();
        workloadInstance.setNamespace(NAMESPACE_WITH_WRONG_FIRST_SYMBOL);

        //Test method
        final Throwable thrown = catchThrowable(() -> {
            HelmAppValidator.validate(workloadInstance);
        });

        //Verify
        assertThat(thrown).isInstanceOf(InvalidInputException.class);
        assertThat(thrown.getMessage()).isNotBlank();
    }

    @Test
    public void givenWorkloadInstanceNamespaceFinishedNonAlphanumeric_whenValidating_thenThrowException() {
        //Init
        final WorkloadInstance workloadInstance = getInstance();
        workloadInstance.setNamespace(NAMESPACE_WITH_WRONG_LAST_SYMBOL);

        //Test method
        final Throwable thrown = catchThrowable(() -> {
            HelmAppValidator.validate(workloadInstance);
        });

        //Verify
        assertThat(thrown).isInstanceOf(InvalidInputException.class);
        assertThat(thrown.getMessage()).isNotBlank();
    }

    private WorkloadInstance getInstance() {

        final WorkloadInstance workloadInstance = new WorkloadInstance();
        workloadInstance.setWorkloadInstanceName("name");
        workloadInstance.setNamespace("7namespace-3-test");
        workloadInstance.setCluster("cluster name");
        workloadInstance.setAdditionalParameters(Map.of("namespace", "test"));

        return workloadInstance;
    }

}