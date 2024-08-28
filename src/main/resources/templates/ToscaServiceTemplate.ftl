#
# COPYRIGHT Ericsson 2023
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

tosca_definitions_version: tosca_simple_yaml_1_3
data_types:
  com.ericsson.oss.app.mgr.acm.datatypes.ToscaConceptIdentifier:
    derived_from: tosca.datatypes.Root
    version: 1.0.0
    properties:
      name:
        type: string
        required: true
      version:
        type: string
        required: true

  com.ericsson.oss.app.mgr.acm.datatypes.artifact:
    derived_from: tosca.datatypes.Root
    version: 1.0.0
    properties:
      name:
        type: string
        required: true
        description: The description of the artifact
      bucketName:
        type: string
        required: true
        description: The Object Store bucket name in which the artifact is stored
      objectName:
        type: string
        required: true
        description: The object name of the artifact in Object Store
      type:
        type: string
        required: true
        description: The type of the artifact

  com.ericsson.oss.app.mgr.acm.datatypes.keycloakParameters:
    derived_from: tosca.datatypes.Root
    version: 1.0.0
    properties:
      keycloakClientId:
        type: string
        required: true
        description: The Keycloak client id
      keycloakClientSecret:
        type: string
        required: true
        description: The Keycloak client secret
      keycloakClientUrl:
        type: string
        required: true
        description: The Keycloak client URL

node_types:
  com.ericsson.oss.app.mgr.acm.Participant:
    version: 1.0.0
    derived_from: tosca.nodetypes.Root
    properties:
      provider:
        type: string
        required: false

  org.onap.policy.clamp.acm.AutomationComposition:
    version: 1.0.0
    derived_from: tosca.nodetypes.Root
    properties:
      provider:
        type: string
        required: false
        metadata:
          common: true
        description: Specifies the organization that provides the Automation Composition element
      elements:
        type: list
        required: true
        metadata:
          common: true
        entry_schema:
          type: com.ericsson.oss.app.mgr.acm.datatypes.ToscaConceptIdentifier
          type_version: 1.0.0
        description: Specifies a list of Automation Composition element definitions that make up this Automation Composition definition

  org.onap.policy.clamp.acm.AutomationCompositionElement:
    version: 1.0.0
    derived_from: tosca.nodetypes.Root
    properties:
      provider:
        type: string
        required: false
        metadata:
          common: true
        description: Specifies the organization that provides the Automation Composition element
      startPhase:
        type: integer
        required: false
        constraints:
          - greater_or_equal: 0
        metadata:
          common: true
        description: A value indicating the start phase in which this Automation Composition element will be started, the
          first start phase is zero. Automation Composition elements are started in their start_phase order and stopped
          in reverse start phase order. Automation Composition elements with the same start phase are started and
          stopped simultaneously
      uninitializedToPassiveTimeout:
        type: integer
        required: false
        constraints:
          - greater_or_equal: 0
        default: 60
        metadata:
          common: true
        description: The maximum time in seconds to wait for a state change from uninitialized to passive
      passiveToRunningTimeout:
        type: integer
        required: false
        constraints:
          - greater_or_equal: 0
        default: 60
        metadata:
          common: true
        description: The maximum time in seconds to wait for a state change from passive to running
      runningToPassiveTimeout:
        type: integer
        required: false
        constraints:
          - greater_or_equal: 0
        default: 60
        metadata:
          common: true
        description: The maximum time in seconds to wait for a state change from running to passive
      passiveToUninitializedTimeout:
        type: integer
        required: false
        constraints:
          - greater_or_equal: 0
        default: 60
        metadata:
          common: true
        description: The maximum time in seconds to wait for a state change from passive to uninitialized
<#list appData.getAppComponents() as appComponent>
<#assign appComponentType = appComponent.getType()>
<#if appComponentTypeComparator.isAsdType(appComponentType)>

  org.onap.policy.clamp.acm.AppMgrAutomationCompositionElement:
    version: 1.0.0
    derived_from: org.onap.policy.clamp.acm.AutomationCompositionElement
    properties:
      artifacts:
        type: list
        required: true
        entry_schema:
          type: com.ericsson.oss.app.mgr.acm.datatypes.artifact
          type_version: 1.0.0
        description: The list of artifacts
      namespace:
        type: string
        required: true
        description: The namespace where the workloadInstance for this Cloud Native LCM microservice will be installed
      timeout:
        type: integer
        required: false
        description: The HFE instantiation timeout. If no value is supplied a default value of 5 minutes is used by HFE
      keycloakParameters:
        type: com.ericsson.oss.app.mgr.acm.datatypes.keycloakParameters
        type_version: 1.0.0
        required: true
        description: The Keycloak parameters for this Cloud Native LCM microservice
      userDefinedHelmParameters:
        type: map
        required: false
        description: The additional parameters for this Cloud Native LCM microservice as a map of string object
<#break>
</#if>
</#list>
<#list appData.getAppComponents() as appComponent>
<#assign appComponentType = appComponent.getType()>
<#if appComponentTypeComparator.isDataManagementType(appComponentType)>

  org.onap.policy.clamp.acm.DataManagementAutomationCompositionElement:
    version: 1.0.0
    derived_from: org.onap.policy.clamp.acm.AutomationCompositionElement
    properties:
      iamClientId:
        type: string
        required: true
        description: The IAM_CLIENT_ID generated for the App.
      artifacts:
        type: list
        required: true
        entry_schema:
          type: com.ericsson.oss.app.mgr.acm.datatypes.artifact
          type_version: 1.0.0
        description: The list of artifacts
<#break>
</#if>
</#list>

topology_template:
  node_templates:
<#list appData.getAppComponents() as appComponent>
<#assign appComponentType = appComponent.getType()>
<#if appComponentTypeComparator.isAsdType(appComponentType)>
    com.ericsson.oss.app.mgr.acm.CloudNativeLcmParticipant:
      version: 1.0.0
      type: com.ericsson.oss.app.mgr.acm.Participant
      type_version: 1.0.0
      description: The participant to support Cloud Native LCM microservices
      properties:
        provider: Ericsson

    <#break>
    </#if>
</#list>
<#list appData.getAppComponents() as appComponent>
<#assign appComponentType = appComponent.getType()>
<#if appComponentTypeComparator.isDataManagementType(appComponentType)>
    com.ericsson.oss.app.mgr.acm.DataManagementLcmParticipant:
      version: 1.0.0
      type: com.ericsson.oss.app.mgr.acm.Participant
      type_version: 1.0.0
      description: The participant to support Data Management App Components
      properties:
        provider: Ericsson

    <#break>
    </#if>
</#list>
<#list appData.getAppComponents() as appComponent>
<#assign appComponentType = appComponent.getType()>
<#if appComponentTypeComparator.isAsdType(appComponentType)>
    ${appComponent.getCompositionElementName()}:
      version: 1.0.0
      type: org.onap.policy.clamp.acm.AppMgrAutomationCompositionElement
      type_version: 1.0.0
      description: The Automation Composition element for the rApp's Cloud Native LCM microservice
      properties:
        provider: Ericsson
        startPhase: 0
        uninitializedToPassiveTimeout: 300
        podStatusCheckInterval: 30
        artifacts:
        <#list appComponent.getArtifacts() as artifact>
          <#assign location = artifact.getLocation()?split("/")>
          - name: ${artifact.getName()}
            bucketName: ${location[0]}
            objectName: ${location[1]}/${location[2]}
            type: ${artifact.getType()}
        </#list>
</#if>
<#if appComponentTypeComparator.isDataManagementType(appComponentType)>

    ${appComponent.getCompositionElementName()}:
      version: ${appComponent.getVersion()}
      type: org.onap.policy.clamp.acm.DataManagementAutomationCompositionElement
      type_version: 1.0.0
      description: The Automation Composition element for the rApp's Data Management component
      properties:
        provider: Ericsson
        startPhase: 0
        uninitializedToPassiveTimeout: 300
        podStatusCheckInterval: 30
</#if>
</#list>
<#assign appProvider = appData.getProvider()>

    com.ericsson.oss.app.mgr.ac.element.AutomationCompositionDefinition:
      version: 1.0.0
      type: org.onap.policy.clamp.acm.AutomationComposition
      type_version: 1.0.0
      description: The Automation Composition definition supporting the deployment of an rApp
      properties:
        provider: ${appProvider}
        elements:
<#list appData.getAppComponents() as appComponent>
<#assign appComponentType = appComponent.getType()>
          - name: ${appComponent.getCompositionElementName()}
<#if appComponentTypeComparator.isAsdType(appComponentType)>
            version: 1.0.0
<#else>
            version: ${appComponent.getVersion()}
</#if>
</#list>