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

<#assign appInstance = compositionInstanceData.getAppInstance()>
<#if compositionInstanceData.getComponentInstancesProperties()??>
<#assign componentInstancesProperties = compositionInstanceData.getComponentInstancesProperties()>
</#if>
name: ${appInstance.getId()}
version: 1.0.0
compositionId: ${appInstance.getApp().getCompositionId()}
<#if appInstance.getCompositionInstanceId()??>
instanceId: ${appInstance.getCompositionInstanceId()}
</#if>
<#if appInstance.getTargetApp()?? && appInstance.getTargetApp().getCompositionId()??>
compositionTargetId: ${appInstance.getTargetApp().getCompositionId()}
</#if>
description: Properties file containing configuration settings for an application instance
elements:
<#list appInstance.getAppComponentInstances() as appComponentInstance>
<#assign appComponentType = appComponentInstance.getAppComponent().getType()?upper_case>
<#if appComponentTypeComparator.isAsdType(appComponentType)>
  ${appComponentInstance.getCompositionElementInstanceId()}:
    id: ${appComponentInstance.getCompositionElementInstanceId()}
    definition:
      name: ${appComponentInstance.getAppComponent().getCompositionElementName()}
      version: 1.0.0
    description: The element instance properties for this Cloud Native LCM microservice
    properties:
      <#assign timeoutValue = defaultTimeout>
      <#assign namespaceValue = defaultNamespace>
      <#if componentInstancesProperties?has_content>
        <#assign matchedComponent = componentInstancesProperties?filter(component -> component.getName()?upper_case == appComponentInstance.getAppComponent().getName()?upper_case)>
        <#if matchedComponent?has_content>
          <#assign componentProps = matchedComponent[0].getProperties()>
          <#if componentProps.timeout??>
            <#assign timeoutValue = componentProps.timeout>
          </#if>
          <#if componentProps.namespace??>
            <#assign namespaceValue = componentProps.namespace>
          </#if>
          <#if componentProps.userDefinedHelmParameters??>
      userDefinedHelmParameters: ${ObjectMapper.writeValueAsString(componentProps.userDefinedHelmParameters)}
          </#if>
        </#if>
      </#if>
      namespace: ${namespaceValue}
      timeout: ${timeoutValue}
      keycloakParameters:
      <#list appInstance.getClientCredentials() as credential>
        keycloakClientId: ${credential.getClientId()}
        keycloakClientSecret: ${credential.getClientSecret()}
        keycloakClientUrl: ${credential.getClientUrl()}
      </#list>
</#if>
<#if appComponentTypeComparator.isDataManagementType(appComponentType)>
<#assign appComponent = appComponentInstance.getAppComponent()>
  ${appComponentInstance.getCompositionElementInstanceId()}:
    id: ${appComponentInstance.getCompositionElementInstanceId()}
    definition:
      name: ${appComponentInstance.getAppComponent().getCompositionElementName()}
      version: ${appComponent.getVersion()}
    description: The instance properties for this Data Management Composition Element
    properties:
      iamClientId: ${iamClientId}
      artifacts:
      <#list appComponent.getArtifacts() as artifact>
        <#assign location = artifact.getLocation()?split("/")>
        - name: ${artifact.getName()}
          bucketName: ${location[0]}
          objectName: ${location[1]}/${location[2]}
          type: ${artifact.getType()}
       </#list>
</#if>
</#list>