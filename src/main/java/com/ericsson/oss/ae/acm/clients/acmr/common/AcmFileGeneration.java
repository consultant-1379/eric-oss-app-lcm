/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

package com.ericsson.oss.ae.acm.clients.acmr.common;

import com.ericsson.oss.ae.acm.clients.acmr.model.CompositionInstanceData;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.persistence.entity.App;

/**
 * Interface for AcmFileGeneration in App LCM.
 */
public interface AcmFileGeneration {
    /**
     * Generates a TOSCA Service Template file in App LCM which is to be sent to the ACM-R service. This method requires
     * an object to be passed. The state of the object will be used to populate a FreeMarker template. The method
     * returns the generated TOSCA Service Template as a String. The output can be used in the body of a REST request to
     * the ACM-R service.
     *
     * @param appDataModel The object whose state is to be used to generate the TOSCA Service Template.
     * @return A String representation of the generated TOSCA Service Template.
     * @throws AppLcmException Thrown if there is an error populating the FreeMarker template.
     */
    String generateToscaServiceTemplate(final App appDataModel) throws AppLcmException;

    /**
     * Generates an AC instance properties file in App LCM which is to be sent to the ACM-R service. This method
     * requires an object to be passed. The state of the object will be used to populate a FreeMarker template.
     * The method returns the generated AC instance properties as a String. The output can be used in the body of a REST
     * request to the ACM-R service.
     *
     * @param compositionInstanceData The object whose state is to be used to generate the AC instance properties.
     * @return A String representation of the generated AC instance properties.
     * @throws AppLcmException Thrown if there is an error populating the FreeMarker template.
     */
    String generateAcmInstancePropertiesFile(final CompositionInstanceData compositionInstanceData) throws AppLcmException;
}
