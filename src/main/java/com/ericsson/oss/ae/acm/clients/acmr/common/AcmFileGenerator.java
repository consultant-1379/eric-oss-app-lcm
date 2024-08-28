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

import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.ACM_FILE_GENERATOR_VERSION;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.AC_INSTANCE_PROPERTIES_FILE_NAME;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.FTL_FILE_EXTENSION;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.OBJECT_MAPPER_SHARED_VARIABLE;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.PATH_TO_FREEMARKER_TEMPLATES_DIRECTORY;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.TOSCA_SERVICE_TEMPLATE_FILE_NAME;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmConstants.YAML_FILE_EXTENSION;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.AC_INSTANCE_PROPERTIES_GENERATION_ERROR;
import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

import com.ericsson.oss.ae.acm.clients.acmr.model.CompositionInstanceData;
import com.ericsson.oss.ae.acm.common.constant.ValidAppComponentType;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppComponent;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;
import com.ericsson.oss.ae.acm.persistence.entity.ClientCredential;

/**
 * Class for an AcmFileGenerator in App LCM.
 */
@Component
public class AcmFileGenerator implements AcmFileGeneration {

    @Value("${NAMESPACE:default}")
    private String defaultNamespace;
    private static final Logger logger = LoggerFactory.getLogger(AcmFileGenerator.class);
    private static final String COMPONENT_TYPE_COMPARATOR_MODEL_KEY = "appComponentTypeComparator";
    private static final String APP_DATA_MODEL_KEY = "appData";
    private static final String COMPOSITION_INSTANCE_DATA_MODEL_KEY = "compositionInstanceData";
    private static final String ASD_PROPERTY_DEFAULT_NS = "defaultNamespace";
    private static final String ASD_PROPERTY_DEFAULT_TIMEOUT = "defaultTimeout";
    private static final String DATA_MANAGEMENT_PROPERTY_IAM_CLIENT_ID = "iamClientId";

    private final Configuration configuration;
    public static final int DEFAULT_TIMEOUT = 5; // Set your constant timeout (5 minutes)

    public AcmFileGenerator() {
        logger.debug("Configuring the AcmFileGenerator version to {} and setting the classpath for template loading to {}...",
            ACM_FILE_GENERATOR_VERSION, PATH_TO_FREEMARKER_TEMPLATES_DIRECTORY);
        this.configuration = new Configuration(ACM_FILE_GENERATOR_VERSION);
    }

    /**
     * Generates a TOSCA service template based on the provided App data model.
     *
     * @param appDataModel The data model of the App for which the TOSCA service template is generated.
     * @return The dynamically generated TOSCA service template content in YAML format.
     * @throws AppLcmException If an error occurs during TOSCA service template generation.
     */
    @Override
    public String generateToscaServiceTemplate(final App appDataModel) {
        try {
            logger.info("Generating TOSCA service template");
            this.configuration.setClassLoaderForTemplateLoading(AcmFileGenerator.class.getClassLoader(), PATH_TO_FREEMARKER_TEMPLATES_DIRECTORY);
            this.configuration.setSharedVariable(OBJECT_MAPPER_SHARED_VARIABLE, configuration.getObjectWrapper().wrap(new ObjectMapper()));
            final Template toscaServiceTemplate = this.configuration.getTemplate(String.format("%s%s", TOSCA_SERVICE_TEMPLATE_FILE_NAME, FTL_FILE_EXTENSION));
            final TemplateHashModel componentTypeComparatorModel = getTypeComparatorForModel();
            final StringWriter stringWriter = new StringWriter();
            final Map<String, Object> templateData = new HashMap<>();
            templateData.put(APP_DATA_MODEL_KEY, appDataModel);
            templateData.put(COMPONENT_TYPE_COMPARATOR_MODEL_KEY, componentTypeComparatorModel);
            toscaServiceTemplate.process(templateData, stringWriter);
            final String templateName = toscaServiceTemplate.getName();
            final String generatedFileName = templateName.replace(FTL_FILE_EXTENSION, YAML_FILE_EXTENSION);
            logger.debug("Generated Tosca file name: {}", generatedFileName);
            final String toscaTemplateYamlString = stringWriter.toString().trim();
            logger.debug("TOSCA service template yaml description: {}", toscaTemplateYamlString);
            return toscaTemplateYamlString;
        } catch (final IOException | TemplateException exception) {
            logger.error("Error occurred during TOSCA Service Template generation", exception);
            throw new AppLcmException(INTERNAL_SERVER_ERROR, TOSCA_SERVICE_TEMPLATE_GENERATION_ERROR);
        }
    }
    /**
     * Generates ACM instance properties file based on provided CompositionInstanceData data.
     *
     * @param compositionInstanceData The data for the composition properties.
     * @return The dynamically generated ACM file content in YAML format.
     * @throws AppLcmException If an error occurs during ACM file generation.
     */
    @Override
    public String generateAcmInstancePropertiesFile(final CompositionInstanceData compositionInstanceData) {
        try {
            logger.info("Generating CompositionInstance properties file");
            this.configuration.setClassLoaderForTemplateLoading(AcmFileGenerator.class.getClassLoader(), PATH_TO_FREEMARKER_TEMPLATES_DIRECTORY);
            this.configuration.setSharedVariable(OBJECT_MAPPER_SHARED_VARIABLE, configuration.getObjectWrapper().wrap(new ObjectMapper()));
            final Template instancePropertiesTemplate = this.configuration.getTemplate(String.format("%s%s", AC_INSTANCE_PROPERTIES_FILE_NAME, FTL_FILE_EXTENSION));
            final StringWriter stringWriter = new StringWriter();
            final Map<String, Object> acInstancePropertiesDataModel = prepareAcInstancePropertiesDataModel(compositionInstanceData);
            instancePropertiesTemplate.process(acInstancePropertiesDataModel, stringWriter);
            final String templateName = instancePropertiesTemplate.getName();
            final String generatedFileName = templateName.replace(FTL_FILE_EXTENSION, YAML_FILE_EXTENSION);
            logger.debug("Generated ACM Instance Properties file name: {}", generatedFileName);
            final String compositionInstancePropertiesYamlString = stringWriter.toString().trim();
            logger.debug("Generated ACM Instance Properties file content: {}", compositionInstancePropertiesYamlString);
            return compositionInstancePropertiesYamlString;
        } catch (final IOException | TemplateException exception) {
            logger.error("Exception while generating AC Instance properties data", exception);
            throw new AppLcmException(INTERNAL_SERVER_ERROR, AC_INSTANCE_PROPERTIES_GENERATION_ERROR);
        }
    }

    /**
     * Builds the data model for processing the composition instance template.
     * @param compositionInstanceData instance data values
     * @return the composition instance data in a Map
     * @throws TemplateModelException if an error during freemarker template handling
     */
    private Map<String, Object> prepareAcInstancePropertiesDataModel(final CompositionInstanceData compositionInstanceData) throws TemplateModelException {
        final Map<String, Object> acInstancePropertiesDataModel = new HashMap<>();
        final TemplateHashModel appComponentTypeModel = getTypeComparatorForModel();
        acInstancePropertiesDataModel.put(COMPOSITION_INSTANCE_DATA_MODEL_KEY, compositionInstanceData);
        acInstancePropertiesDataModel.put(COMPONENT_TYPE_COMPARATOR_MODEL_KEY, appComponentTypeModel);

        final List<AppComponent> componentList = compositionInstanceData.getAppInstance().getApp().getAppComponents();
        for (final AppComponent component : componentList) {
            if (AppComponentTypeComparator.isAsdType(component.getType())){
                acInstancePropertiesDataModel.put(ASD_PROPERTY_DEFAULT_NS, defaultNamespace);
                acInstancePropertiesDataModel.put(ASD_PROPERTY_DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);

            } else if (AppComponentTypeComparator.isDataManagementType(component.getType())){
                final AppInstances appInstance = compositionInstanceData.getAppInstance();
                List<ClientCredential> credentials = appInstance.getClientCredentials();
                final String clientId = credentials.get(0).getClientId();
                acInstancePropertiesDataModel.put(DATA_MANAGEMENT_PROPERTY_IAM_CLIENT_ID, clientId);

            }
        }
        return acInstancePropertiesDataModel;
    }

    /**
     * Wraps the AppComponentTypeComparator class to allow its methods to be called from the freemarker template.
     * @return the comparator class as a TemplateHashModel type
     * @throws TemplateModelException if an error during freemarker template handling
     */
    private TemplateHashModel getTypeComparatorForModel() throws TemplateModelException {
        final BeansWrapper templateBeansWrapper = new BeansWrapperBuilder(ACM_FILE_GENERATOR_VERSION).build();
        final TemplateHashModel staticModels = templateBeansWrapper.getStaticModels();
        return (TemplateHashModel)staticModels.get(AppComponentTypeComparator.class.getCanonicalName());
    }

}
