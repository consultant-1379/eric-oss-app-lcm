{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-oss-app-lcm.name" }}
  {{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-oss-app-lcm.version" }}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Expand the name of the chart.
*/}}
{{- define "eric-oss-app-lcm.fullname" -}}
{{- if .Values.fullnameOverride -}}
  {{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
  {{- $name := default .Chart.Name .Values.nameOverride -}}
  {{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
    Define Image Pull Policy
*/}}
{{- define "eric-oss-app-lcm.registryImagePullPolicy" -}}
    {{- $globalRegistryPullPolicy := "IfNotPresent" -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.imagePullPolicy -}}
                {{- $globalRegistryPullPolicy = .Values.global.registry.imagePullPolicy -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- print $globalRegistryPullPolicy -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-oss-app-lcm.chart" }}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create image pull secrets for global (outside of scope)
*/}}
{{- define "eric-oss-app-lcm.pullSecret.global" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global -}}
  {{- if .Values.global.pullSecret -}}
    {{- $pullSecret = .Values.global.pullSecret -}}
  {{- end -}}
  {{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence
*/}}
{{- define "eric-oss-app-lcm.pullSecret" -}}
{{- $pullSecret := (include "eric-oss-app-lcm.pullSecret.global" . ) -}}
{{- if .Values.imageCredentials -}}
  {{- if .Values.imageCredentials.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
  {{- end -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{- define "eric-oss-app-lcm.mainImagePath" -}}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := (index $productInfo "images" "eric-oss-app-lcm" "registry") -}}
    {{- $repoPath := (index $productInfo "images" "eric-oss-app-lcm" "repoPath") -}}
    {{- $name := (index $productInfo "images" "eric-oss-app-lcm" "name") -}}
    {{- $tag := (index $productInfo "images" "eric-oss-app-lcm" "tag") -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.url -}}
                {{- $registryUrl = .Values.global.registry.url -}}
            {{- end -}}
            {{- if not (kindIs "invalid" .Values.global.registry.repoPath) -}}
                {{- $repoPath = .Values.global.registry.repoPath -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials -}}
        {{- if (index .Values "imageCredentials" "eric-oss-app-lcm") -}}
            {{- if (index .Values "imageCredentials" "eric-oss-app-lcm" "registry") -}}
                {{- if (index .Values "imageCredentials" "eric-oss-app-lcm" "registry" "url") -}}
                    {{- $registryUrl = (index .Values "imageCredentials" "eric-oss-app-lcm" "registry" "url") -}}
                {{- end -}}
            {{- end -}}
            {{- if not (kindIs "invalid" (index .Values "imageCredentials" "eric-oss-app-lcm" "repoPath")) -}}
                {{- $repoPath = (index .Values "imageCredentials" "eric-oss-app-lcm" "repoPath") -}}
            {{- end -}}
        {{- end -}}
        {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
            {{- $repoPath = .Values.imageCredentials.repoPath -}}
        {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
        {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{- define "eric-oss-app-lcm.initDbImagePath" -}}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := (index $productInfo "images" "kubeclient" "registry") -}}
    {{- $repoPath := (index $productInfo "images" "kubeclient" "repoPath") -}}
    {{- $name := (index $productInfo "images" "kubeclient" "name") -}}
    {{- $tag := (index $productInfo "images" "kubeclient" "tag") -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.url -}}
                {{- $registryUrl = .Values.global.registry.url -}}
            {{- end -}}
            {{- if not (kindIs "invalid" .Values.global.registry.repoPath) -}}
                {{- $repoPath = .Values.global.registry.repoPath -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials -}}
        {{- if (index .Values "imageCredentials" "kubeclient") -}}
            {{- if (index .Values "imageCredentials" "kubeclient" "registry") -}}
                {{- if (index .Values "imageCredentials" "kubeclient" "registry" "url") -}}
                    {{- $registryUrl = (index .Values "imageCredentials" "kubeclient" "registry" "url") -}}
                {{- end -}}
            {{- end -}}
            {{- if not (kindIs "invalid" (index .Values "imageCredentials" "kubeclient" "repoPath")) -}}
                {{- $repoPath = (index .Values "imageCredentials" "kubeclient" "repoPath") -}}
            {{- end -}}
        {{- end -}}
        {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
            {{- $repoPath = .Values.imageCredentials.repoPath -}}
        {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
        {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{- define "eric-oss-app-lcm.testImagePath" }}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := (index $productInfo "images" "eric-oss-app-lcmTest" "registry") -}}
    {{- $repoPath := (index $productInfo "images" "eric-oss-app-lcmTest" "repoPath") -}}
    {{- $name := (index $productInfo "images" "eric-oss-app-lcmTest" "name") -}}
    {{- $tag := (index $productInfo "images" "eric-oss-app-lcmTest" "tag") -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.url -}}
                {{- $registryUrl = .Values.global.registry.url -}}
            {{- end -}}
            {{- if not (kindIs "invalid" .Values.global.registry.repoPath) -}}
                {{- $repoPath = .Values.global.registry.repoPath -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials -}}
        {{- if (index .Values "imageCredentials" "eric-oss-app-lcmTest") -}}
            {{- if (index .Values "imageCredentials" "eric-oss-app-lcmTest" "registry") -}}
                {{- if (index .Values "imageCredentials" "eric-oss-app-lcmTest" "registry" "url") -}}
                    {{- $registryUrl = (index .Values "imageCredentials" "eric-oss-app-lcmTest" "registry" "url") -}}
                {{- end -}}
            {{- end -}}
            {{- if not (kindIs "invalid" (index .Values "imageCredentials" "eric-oss-app-lcmTest" "repoPath")) -}}
                {{- $repoPath = (index .Values "imageCredentials" "eric-oss-app-lcmTest" "repoPath") -}}
            {{- end -}}
        {{- end -}}
        {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
            {{- $repoPath = .Values.imageCredentials.repoPath -}}
        {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
        {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{- define "eric-oss-app-lcm.eric-oss-app-ui" }}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := (index $productInfo "images" "eric-oss-app-ui" "registry") -}}
    {{- $repoPath := (index $productInfo "images" "eric-oss-app-ui" "repoPath") -}}
    {{- $name := (index $productInfo "images" "eric-oss-app-ui" "name") -}}
    {{- $tag := (index $productInfo "images" "eric-oss-app-ui" "tag") -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.url -}}
                {{- $registryUrl = .Values.global.registry.url -}}
            {{- end -}}
            {{- if not (kindIs "invalid" .Values.global.registry.repoPath) -}}
                {{- $repoPath = .Values.global.registry.repoPath -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials -}}
        {{- if (index .Values "imageCredentials" "eric-oss-app-ui") -}}
            {{- if (index .Values "imageCredentials" "eric-oss-app-ui" "registry") -}}
                {{- if (index .Values "imageCredentials" "eric-oss-app-ui" "registry" "url") -}}
                    {{- $registryUrl = (index .Values "imageCredentials" "eric-oss-app-ui" "registry" "url") -}}
                {{- end -}}
            {{- end -}}
            {{- if not (kindIs "invalid" (index .Values "imageCredentials" "eric-oss-app-ui" "repoPath")) -}}
                {{- $repoPath = (index .Values "imageCredentials" "eric-oss-app-ui" "repoPath") -}}
            {{- end -}}
        {{- end -}}
        {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
            {{- $repoPath = .Values.imageCredentials.repoPath -}}
        {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
        {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Timezone variable
*/}}
{{- define "eric-oss-app-lcm.timezone" }}
  {{- $timezone := "UTC" }}
  {{- if .Values.global }}
    {{- if .Values.global.timezone }}
      {{- $timezone = .Values.global.timezone }}
    {{- end }}
  {{- end }}
  {{- print $timezone | quote }}
{{- end -}}

{{/*
Helm and Kubernetes labels
*/}}
{{- define "eric-oss-app-lcm.helmK8s-labels" }}
app.kubernetes.io/name: {{ include "eric-oss-app-lcm.name" . }}
helm.sh/chart: {{ include "eric-oss-app-lcm.chart" . }}
{{ include "eric-oss-app-lcm.selectorLabels" . }}
app.kubernetes.io/version: {{ include "eric-oss-app-lcm.version" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
{{/* app.kubernetes.io/managed-by: {{ .Release.Service }} */}}
{{- end -}}

{{/*
Create Ericsson product app.kubernetes.io info
*/}}
{{- define "eric-oss-app-lcm.kubernetes-io-info" -}}
app.kubernetes.io/name: {{ .Chart.Name | quote }}
app.kubernetes.io/version: {{ .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- end -}}

{{/*
Return the fsgroup set via global parameter if it's set, otherwise 10000
*/}}
{{- define "eric-oss-app-lcm.fsGroup.coordinated" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.fsGroup -}}
      {{- if .Values.global.fsGroup.manual -}}
        {{ .Values.global.fsGroup.manual }}
      {{- else -}}
        {{- if eq .Values.global.fsGroup.namespace true -}}
          # The 'default' defined in the Security Policy will be used.
        {{- else -}}
          10000
      {{- end -}}
    {{- end -}}
  {{- else -}}
    10000
  {{- end -}}
  {{- else -}}
    10000
  {{- end -}}
{{- end -}}

{{/*
Standard UI labels
*/}}
{{- define "eric-oss-app-lcm.standard-ui-labels" -}}
ui.ericsson.com/part-of: workspace-gui
{{- end -}}

{{/*
Common labels
*/}}
{{- define "eric-oss-app-lcm.labels" -}}
  {{- $helmK8sLabels := include "eric-oss-app-lcm.helmK8s-labels" . | fromYaml -}}
  {{- $globalLabels := (.Values.global).labels -}}
  {{- $serviceLabels := .Values.labels -}}
  {{- $uiLabels := include "eric-oss-app-lcm.standard-ui-labels" . | fromYaml -}}
  {{- include "eric-oss-app-lcm.mergeLabels" (dict "location" .Template.Name "sources" (list $helmK8sLabels $globalLabels $serviceLabels $uiLabels)) | trim }}
{{- end -}}

{{/*
App labels
*/}}
{{- define "eric-oss-app-lcm.appLabels" -}}
{{- include "eric-oss-app-lcm.kubernetes-io-info" . }}
app: {{ include "eric-oss-app-lcm.name" . }}
chart: {{ include "eric-oss-app-lcm.chart" . }}
release: {{ .Release.Name | quote }}
heritage: {{ .Release.Service | quote }}
{{- end -}}

{{/*
Standard UI annotations
*/}}
{{- define "eric-oss-app-lcm.standard-ui-annotations" -}}
ui.ericsson.com/port: {{ .Values.service.frontendPort | quote }}
{{ $serviceMesh := include "eric-oss-app-lcm.service-mesh-enabled" . | trim }}
{{ $tls := include "eric-oss-app-lcm.global-security-tls-enabled" . | trim }}
{{ if and (eq $serviceMesh "true") (eq $tls "true") }}
ui.ericsson.com/protocol: "https"
{{- else -}}
ui.ericsson.com/protocol: "http"
{{- end -}}
{{- end -}}

{{/*
Release Annotations
*/}}
{{- define "eric-oss-app-lcm.releaseAnnotations" -}}
meta.helm.sh/release-name: {{ .Release.Name | quote }}
meta.helm.sh/release-namespace: {{ .Release.Namespace | quote }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "eric-oss-app-lcm.selectorLabels" -}}
app: {{ include "eric-oss-app-lcm.name" . }}
app.kubernetes.io/name: {{ include "eric-oss-app-lcm.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
eric-data-object-storage-mn-access: "true"
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "eric-oss-app-lcm.serviceAccountName" -}}
  {{- if .Values.serviceAccount.create }}
    {{- default (include "eric-oss-app-lcm.fullname" .) .Values.serviceAccount.name }}
  {{- else }}
    {{- default "default" .Values.serviceAccount.name }}
  {{- end }}
{{- end }}

{{/*
Annotations for Product Name and Product Number (DR-D1121-064).
*/}}
{{- define "eric-oss-app-lcm.product-info" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end }}

{{/*
Common annotation
*/}}
{{- define "eric-oss-app-lcm.annotations" }}
  {{- $productInfoAnn := include "eric-oss-app-lcm.product-info" . | fromYaml -}}
  {{- $globalAnn := (.Values.global).annotations -}}
  {{- $serviceAnn := .Values.annotations -}}
  {{- $uiPort := include "eric-oss-app-lcm.standard-ui-annotations" . | fromYaml -}}
  {{- include "eric-oss-app-lcm.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfoAnn $globalAnn $serviceAnn $uiPort)) | trim }}
{{- end}}

{{/*
Test annotations
*/}}
{{- define "eric-oss-app-lcm.testAnnotations" -}}
  {{- $helmHooks := dict "helm.sh/hook" "test-success" -}}
  {{- $commonAnn := include "eric-oss-app-lcm.annotations" . | fromYaml }}
  {{- include "eric-oss-app-lcm.mergeAnnotations" (dict "location" .Template.Name "sources" (list $helmHooks $commonAnn)) | trim | nindent 4 }}
{{- end -}}

{{/*
Annotations with Prometheus
*/}}
{{- define "eric-oss-app-lcm.annotationsWithPrometheus" -}}
  {{- $prometheus := include "eric-oss-app-lcm.prometheus" .| fromYaml }}
  {{- $commonAnnotations := include "eric-oss-app-lcm.annotations" . | fromYaml }}
  {{- include "eric-oss-app-lcm.mergeAnnotations" (dict "location" .Template.Name "sources" (list $prometheus $commonAnnotations )) | trim | nindent 4 }}
{{- end -}}

{{/*
Create prometheus info
*/}}
{{- define "eric-oss-app-lcm.prometheus" -}}
prometheus.io/path: {{ .Values.prometheus.path | quote }}
prometheus.io/port: {{ .Values.service.port | quote }}
prometheus.io/scrape: {{ .Values.prometheus.scrape | quote }}
prometheus.io/scrape-role: {{ .Values.prometheus.role | quote }}
prometheus.io/scrape-interval: {{ .Values.prometheus.interval | quote }}
{{- end -}}

{{/*
Define the role reference for security policy
*/}}
{{- define "eric-oss-app-lcm.securityPolicy.reference" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.security -}}
      {{- if .Values.global.security.policyReferenceMap -}}
        {{ $mapped := index .Values "global" "security" "policyReferenceMap" "default-restricted-security-policy" }}
        {{- if $mapped -}}
          {{ $mapped }}
        {{- else -}}
          default-restricted-security-policy
        {{- end -}}
      {{- else -}}
        default-restricted-security-policy
      {{- end -}}
    {{- else -}}
      default-restricted-security-policy
    {{- end -}}
  {{- else -}}
    default-restricted-security-policy
  {{- end -}}
{{- end -}}

{{/*
Define the annotations for security policy
*/}}
{{- define "eric-oss-app-lcm.securityPolicy.annotations" -}}
# Automatically generated annotations for documentation purposes.
{{- end -}}

{{/*
Define Pod Disruption Budget value taking into account its type (int or string)
*/}}
{{- define "eric-oss-app-lcm.pod-disruption-budget" -}}
  {{- if kindIs "string" .Values.podDisruptionBudget.minAvailable -}}
    {{- print .Values.podDisruptionBudget.minAvailable | quote -}}
  {{- else -}}
    {{- print .Values.podDisruptionBudget.minAvailable | atoi -}}
  {{- end -}}
{{- end -}}

{{/*
Define upper limit for TerminationGracePeriodSeconds
*/}}
{{- define "eric-oss-app-lcm.terminationGracePeriodSeconds" -}}
{{- if .Values.terminationGracePeriodSeconds -}}
  {{- toYaml .Values.terminationGracePeriodSeconds -}}
{{- end -}}
{{- end -}}

{{/*
Define upper limit for TerminationGracePeriodSeconds
*/}}
{{- define "eric-oss-app-lcm.tolerations" -}}
{{- if .Values.tolerations -}}
  {{- toYaml .Values.tolerations -}}
{{- end -}}
{{- end -}}

{{/*
Define application Id
*/}}
{{- define "eric-oss-app-lcm.application" -}}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- index $productInfo "images" "eric-oss-app-lcm" "name" -}}
{{- end -}}

{{/*
Create a merged set of nodeSelectors from global and service level.
*/}}
{{- define "eric-oss-app-lcm.nodeSelector" -}}
{{- $globalValue := (dict) -}}
{{- if .Values.global -}}
    {{- if .Values.global.nodeSelector -}}
      {{- $globalValue = .Values.global.nodeSelector -}}
    {{- end -}}
{{- end -}}
{{- if .Values.nodeSelector -}}
  {{- range $key, $localValue := .Values.nodeSelector -}}
    {{- if hasKey $globalValue $key -}}
         {{- $Value := index $globalValue $key -}}
         {{- if ne $Value $localValue -}}
           {{- printf "nodeSelector \"%s\" is specified in both global (%s: %s) and service level (%s: %s) with differing values which is not allowed." $key $key $globalValue $key $localValue | fail -}}
         {{- end -}}
     {{- end -}}
    {{- end -}}
    {{- toYaml (merge $globalValue .Values.nodeSelector) | trim | nindent 2 -}}
{{- else -}}
  {{- if not ( empty $globalValue ) -}}
    {{- toYaml $globalValue | trim | nindent 2 -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
Env variables values
*/}}
{{- define "eric-oss-app-lcm.envVarSessionId" }}
  {{- default .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Read existing db secret
  - If the existing secret has username it means it uses this secret for the db
  - else it will use the new db secret custom-user
*/}}
{{- define "eric-oss-app-lcm.dbSecretName" -}}
{{- $secretName := "" -}}
{{- $existingSecret := (lookup "v1" "Secret" .Release.Namespace .Chart.Name).data -}}
  {{- if and $existingSecret (hasKey $existingSecret "username")  -}}
    {{- $secretName = .Chart.Name -}}
  {{- else -}}
    {{- $secretName = (index .Values "eric-data-document-db" "credentials" "kubernetesSecretName") -}}
  {{- end -}}
{{- $secretName -}}
{{- end -}}


{{/*
DR-D470217-007-AD This helper defines whether this service enter the Service Mesh or not.
*/}}
{{- define "eric-oss-app-lcm.service-mesh-enabled" }}
  {{- $globalMeshEnabled := "false" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.serviceMesh -}}
        {{- $globalMeshEnabled = .Values.global.serviceMesh.enabled -}}
    {{- end -}}
  {{- end -}}
  {{- $globalMeshEnabled -}}
{{- end -}}


{{/*
DR-D470217-011 This helper defines the annotation which bring the service into the mesh.
*/}}
{{- define "eric-oss-app-lcm.service-mesh-inject" }}
{{- if eq (include "eric-oss-app-lcm.service-mesh-enabled" .) "true" }}
sidecar.istio.io/inject: "true"
{{- else -}}
sidecar.istio.io/inject: "false"
{{- end -}}
{{- end -}}

{{/*
This helper defines the annotation which adds hooks to delay application startup until the pod proxy is ready to accept traffic
*/}}
{{- define "eric-oss-app-lcm.istio-proxy-config-annotation" }}
{{- if eq (include "eric-oss-app-lcm.service-mesh-enabled" .) "true" }}
proxy.istio.io/config: '{ "holdApplicationUntilProxyStarts": true }'
{{- end -}}
{{- end -}}

{{/*
GL-D470217-080-AD
This helper captures the service mesh version from the integration chart to
annotate the workloads so they are redeployed in case of service mesh upgrade.
*/}}
{{- define "eric-oss-app-lcm.service-mesh-version" }}
{{- if eq (include "eric-oss-app-lcm.service-mesh-enabled" .) "true" }}
  {{- if .Values.global -}}
    {{- if .Values.global.serviceMesh -}}
      {{- if .Values.global.serviceMesh.annotations -}}
        {{ .Values.global.serviceMesh.annotations | toYaml }}
      {{- end -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
check global.security.tls.enabled
*/}}
{{- define "eric-oss-app-lcm.global-security-tls-enabled" -}}
{{- if  .Values.global -}}
  {{- if  .Values.global.security -}}
    {{- if  .Values.global.security.tls -}}
      {{- .Values.global.security.tls.enabled | toString -}}
    {{- else -}}
      {{- "false" -}}
    {{- end -}}
  {{- else -}}
    {{- "false" -}}
  {{- end -}}
{{- else -}}
  {{- "false" -}}
{{- end -}}
{{- end -}}

{{/*
This helper defines the annotation for define service mesh volume
*/}}
{{- define "eric-oss-app-lcm.service-mesh-volume" }}
{{- if and (eq (include "eric-oss-app-lcm.service-mesh-enabled" .) "true") (eq (include "eric-oss-app-lcm.global-security-tls-enabled" .) "true") }}
sidecar.istio.io/userVolume: '{"eric-oss-app-lcm-keycloak-certs-tls":{"secret":{"secretName":"eric-oss-app-lcm-keycloak-secret","optional":true}},"app-lcm-appmgr-data-pg-db-certs-tls":{"secret":{"secretName":"eric-oss-app-lcm-data-pg-db-secret","optional":true}},"eric-oss-app-lcm-certs-ca-tls":{"secret":{"secretName":"eric-sec-sip-tls-trusted-root-cert"}},"eric-oss-app-lcm-eric-data-object-storage-mn-tls":{"secret":{"secretName":"eric-oss-app-lcm-eric-data-object-storage-mn-secret","optional":true}},"eric-oss-app-lcm-eric-dst-collector-certs-tls":{"secret":{"secretName":"eric-oss-app-lcm-eric-dst-collector-secret","optional":true}},"eric-oss-app-lcm-eric-adp-gui-aggregator-service-tls":{"secret":{"secretName":"eric-oss-app-lcm-eric-adp-gui-aggregator-service-secret","optional":true}},  "eric-adp-gui-aggregator-service-internal-ui-client-ca":{"secret":{"secretName":"eric-adp-gui-aggregator-service-internal-ui-client-ca","optional":true}}}'
sidecar.istio.io/userVolumeMount: '{"eric-oss-app-lcm-keycloak-certs-tls":{"mountPath":"/etc/istio/tls/eric-sec-access-mgmt-http/","readOnly":true},"app-lcm-appmgr-data-pg-db-certs-tls":{"mountPath":"/etc/istio/tls/appmgr-data-document-db/","readOnly":true},"eric-oss-app-lcm-certs-ca-tls":{"mountPath":"/etc/istio/tls-ca/","readOnly":true},"eric-oss-app-lcm-eric-data-object-storage-mn-tls":{"mountPath":"/etc/istio/tls/eric-data-object-storage-mn","readOnly":true},"eric-oss-app-lcm-eric-dst-collector-certs-tls":{"mountPath":"/etc/istio/tls/eric-dst-collector/","readOnly":true},"eric-oss-app-lcm-eric-adp-gui-aggregator-service-tls":{"mountPath":"/etc/istio/tls/eric-adp-gui-aggregator-service/","readOnly":true},"eric-adp-gui-aggregator-service-internal-ui-client-ca":{"mountPath":"/etc/istio/tls/eric-adp-gui-aggregator-service-ca/","readOnly":true}}'
{{ end }}
{{- end -}}

{{/*
This helper defines the secret for sip-tls
*/}}
{{- define "eric-oss-app-lcm-keycloak.secret" }}
apiVersion: siptls.sec.ericsson.com/v1
kind: InternalCertificate
metadata:
  name: {{ include "eric-oss-app-lcm.name" . }}-keycloak-int-cert
  labels:
  {{- include "eric-oss-app-lcm.helmK8s-labels" .| nindent 4 }}
  annotations:
  {{- include "eric-oss-app-lcm.product-info" .| nindent 4 }}
spec:
  kubernetes:
    generatedSecretName: {{ include "eric-oss-app-lcm.name" . }}-keycloak-secret
    certificateName: "cert.pem"
    privateKeyName: "key.pem"
  certificate:
    subject:
      cn: {{ include "eric-oss-app-lcm.name" . }}
    extendedKeyUsage:
      tlsClientAuth: true
      tlsServerAuth: false
{{- end -}}

{{- define "eric-oss-app-lcm-eric-adp-gui-aggregator-service-secret" }}
apiVersion: siptls.sec.ericsson.com/v1
kind: InternalCertificate
metadata:
  name: {{ include "eric-oss-app-lcm.name" . }}-eric-adp-gui-aggregator-service-int-cert
  labels:
  {{- include "eric-oss-app-lcm.helmK8s-labels" .| nindent 4 }}
  annotations:
  {{- include "eric-oss-app-lcm.product-info" .| nindent 4 }}

spec:
  kubernetes:
    generatedSecretName: {{ include "eric-oss-app-lcm.name" . }}-eric-adp-gui-aggregator-service-secret
    certificateName: "cert.pem"
    privateKeyName: "key.pem"
  certificate:
    subject:
      cn: {{ include "eric-oss-app-lcm.name" . }}
    extendedKeyUsage:
      tlsClientAuth: true
      tlsServerAuth: true
{{ end }}


{{/*
Define the log streaming method (DR-470222-010)
*/}}
{{- define "eric-oss-app-lcm.streamingMethod" -}}
{{- $streamingMethod := "direct" -}}
{{- if .Values.global -}}
  {{- if .Values.global.log -}}
      {{- if .Values.global.log.streamingMethod -}}
        {{- $streamingMethod = .Values.global.log.streamingMethod }}
      {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.log -}}
  {{- if .Values.log.streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod }}
  {{- end -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}

{{/*
Define the label needed for reaching eric-log-transformer (DR-470222-010)
*/}}
{{- define "eric-oss-app-lcm.directStreamingLabel" -}}
{{- $streamingMethod := (include "eric-oss-app-lcm.streamingMethod" .) -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) }}
logger-communication-type: "direct"
{{- end -}}
{{- end -}}

{{/*
Define logging environment variables (DR-470222-010)
*/}}
{{ define "eric-oss-app-lcm.loggingEnv" }}
{{- $streamingMethod := (include "eric-oss-app-lcm.streamingMethod" .) -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) -}}
  {{- if eq "direct" $streamingMethod }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-http.xml"
  {{- end }}
  {{- if eq "dual" $streamingMethod }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-dual.xml"
  {{- end }}
- name: LOGSTASH_DESTINATION
  value: eric-log-transformer
- name: LOGSTASH_PORT
  value: "9080"
- name: POD_NAME
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
- name: POD_UID
  valueFrom:
    fieldRef:
      fieldPath: metadata.uid
- name: CONTAINER_NAME
  value: eric-oss-app-lcm
- name: NODE_NAME
  valueFrom:
    fieldRef:
      fieldPath: spec.nodeName
- name: NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
{{- else if eq $streamingMethod "indirect" }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-json.xml"
{{- else }}
  {{- fail ".log.streamingMethod unknown" }}
{{- end -}}
{{ end }}

{{/*
This helper defines which out-mesh services are reached by the eric-oss-app-lcm.
*/}}
{{- define "eric-oss-app-lcm.service-mesh-ism2osm-labels" }}
{{- if eq (include "eric-oss-app-lcm.service-mesh-enabled" .) "true" }}
  {{- if eq (include "eric-oss-app-lcm.global-security-tls-enabled" .) "true" }}
eric-sec-access-mgmt-ism-access: "true"
eric-appmgr-data-document-db: "true"
eric-data-object-storage-mn-ism-access: "true"
eric-dst-collector-ism-access: "true"
  {{- end }}
{{- end -}}
{{- end -}}

{{/*
This helper get the custom user used to connect to postgres DB instance.
*/}}
{{ define "eric-oss-app-lcm.dbuser" }}
  {{- $credentials := index .Values "eric-data-document-db" "credentials" "kubernetesSecretName"  -}}
  {{- $secret := (lookup "v1" "Secret" .Release.Namespace $credentials) -}}
  {{- if $secret -}}
    {{ index $secret.data "custom-user" | b64dec | quote }}
  {{- else -}}
    {{- (randAlphaNum 16) | b64enc | quote -}}
  {{- end -}}
{{- end -}}

{{/*
Define eric-oss-app-lcm.apparmor-annotations DR-D1123-127
*/}}
{{- define "eric-oss-app-lcm.apparmor-annotations" }}
{{- $appArmorValue := .Values.appArmorProfile.type -}}
    {{- if .Values.appArmorProfile -}}
        {{- if .Values.appArmorProfile.type -}}
            {{- if eq .Values.appArmorProfile.type "localhost" -}}
                {{- $appArmorValue = printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile }}
            {{- end}}
container.apparmor.security.beta.kubernetes.io/eric-oss-app-lcm: {{ $appArmorValue | quote }}
        {{- end}}
    {{- end}}
{{- end}}

{{/*
Define seccomp profiles DR-D1123-128
*/}}
{{- define "eric-oss-app-lcm.seccomp-profile" }}
    {{- if .Values.seccompProfile }}
      {{- if .Values.seccompProfile.type }}
          {{- if eq .Values.seccompProfile.type "Localhost" }}
              {{- if .Values.seccompProfile.localhostProfile }}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
  localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
              {{- end }}
          {{- else }}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
          {{- end }}
      {{- end }}
    {{- end }}
{{- end }}

{{/*
Define RoleBinding (DR-1123-134)
*/}}
{{- define "eric-oss-app-lcm.securityPolicy.rolekind" -}}
  {{- $roleKind := "" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.securityPolicy -}}
      {{- if .Values.global.securityPolicy.rolekind -}}
        {{- if or (eq "Role" (.Values.global.securityPolicy).rolekind) (eq "ClusterRole" (.Values.global.securityPolicy).rolekind) -}}
          {{- $roleKind = .Values.global.securityPolicy.rolekind -}}
        {{- end -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- print $roleKind -}}
{{- end -}}

{{- define "eric-oss-app-lcm.securityPolicy.rolename" -}}
{{- $rolename := (include "eric-oss-app-lcm.name" .) -}}
{{- if .Values.securityPolicy -}}
  {{- if .Values.securityPolicy.rolename -}}
      {{- $rolename = .Values.securityPolicy.rolename -}}
  {{- end -}}
{{- end -}}
{{- $rolename -}}
{{- end -}}

{{/*
Create Security Policy RoleBinding name for ServiceAccount
*/}}
{{- define "eric-oss-app-lcm.securityPolicy-rolebinding-name" -}}
{{- $serviceAccountName := include "eric-oss-app-lcm.serviceAccountName" . -}}
{{- $rolecipher := substr 0 1 (include "eric-oss-app-lcm.securityPolicy.rolekind" . | toString | lower) -}}
{{- $rolename := include "eric-oss-app-lcm.securityPolicy.rolename" . -}}
{{- printf "%s-%s-%s-sp" $serviceAccountName $rolecipher $rolename -}}
{{- end -}}

{{/*
Define the secret key reference for Object Storage
*/}}
{{- define "eric-oss-app-lcm.secret-key-reference" }}
{{- if (lookup "v1" "Secret" .Release.Namespace "eric-data-object-storage-mn-secret") }}
    {{- print "eric-data-object-storage-mn-secret" -}}
{{- else }}
    {{- print "eric-eo-object-store-cred" -}}
{{- end -}}
{{- end -}}

{{- define "eric-oss-app-lcm-eric-data-object-storage-mn-secret" }}
apiVersion: siptls.sec.ericsson.com/v1
kind: InternalCertificate
metadata:
  name: {{ include "eric-oss-app-lcm.name" . }}-eric-data-object-storage-mn-int-cert
  labels:
  {{- include "eric-oss-app-lcm.labels" .| nindent 4 }}
  annotations:
  {{- include "eric-oss-app-lcm.product-info" .| nindent 4 }}
spec:
  kubernetes:
    generatedSecretName: {{ include "eric-oss-app-lcm.name" . }}-eric-data-object-storage-mn-secret
    certificateName: "cert.pem"
    privateKeyName: "key.pem"
  certificate:
    subject:
      cn: {{ include "eric-oss-app-lcm.name" . }}
    issuer:
      reference: eric-data-object-storage-mn-ca
    extendedKeyUsage:
      tlsClientAuth: true
      tlsServerAuth: true
{{- end -}}

{{/*
The name of the cluster role used during Openshift deployments.
This helper is provided to allow use of the new global.security.privilegedPolicyClusterRoleName if set, otherwise
use the previous naming convention of <release_name>-allowed-use-privileged-policy for backwards compatibility.
*/}}
{{- define "eric-oss-app-lcm.privileged.cluster.role.name" -}}
{{- $privilegedClusterRoleName := printf "%s%s" (include "eric-oss-app-lcm.name" . ) "-allowed-use-privileged-policy" -}}
{{- if .Values.global -}}
  {{- if .Values.global.security -}}
    {{- if hasKey (.Values.global.security) "privilegedPolicyClusterRoleName" -}}
      {{- $privilegedClusterRoleName =  .Values.global.security.privilegedPolicyClusterRoleName }}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- printf "%s" $privilegedClusterRoleName -}}
{{- end -}}

{{- define "eric-oss-app-lcm.registryUrl" -}}
  {{- $g := fromJson (include "eric-oss-app-lcm.global" .) -}}
  {{- $registryUrl := $g.registry.url -}}
  {{- if .Values.global -}}
    {{- if .Values.global.registry -}}
      {{- if .Values.global.registry.url -}}
        {{- $registryUrl := .Values.global.registry.url -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- print $registryUrl }}
{{- end -}}

{{- define "eric-oss-app-lcm.imagePullPolicy" -}}
  {{- $g := fromJson (include "eric-oss-app-lcm.global" .) -}}
  {{- $imagePullPolicy := $g.registry.imagePullPolicy -}}
  {{- if .Values.global -}}
    {{- if .Values.global.registry -}}
      {{- if .Values.global.registry.imagePullPolicy -}}
        {{- $imagePullPolicy := .Values.global.registry.imagePullPolicy -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- print $imagePullPolicy }}
{{- end -}}

{{/*
check global.eric-oss-app-lcm-stub.enabled
*/}}
{{- define "eric-oss-app-lcm.app-lcm-stub-enabled" -}}
{{- $g := fromJson (include "eric-oss-app-lcm.global" .) -}}
{{- $stubEnabled := (index $g "eric-oss-app-lcm-stub" "enabled") -}}
{{- if (index .Values "global") -}}
  {{- if (index .Values "global" "eric-oss-app-lcm-stub") -}}
    {{- if (index .Values "global" "eric-oss-app-lcm-stub" "enabled") -}}
      {{- $stubEnabled := (index .Values "global" "eric-oss-app-lcm-stub" "enabled") -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- print "%t" $stubEnabled -}}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-oss-app-lcm.global" }}
  {{- $globalDefaults := dict "registry" (dict "url" "armdocker.rnd.ericsson.se") -}}
  {{- $globalDefaults := merge $globalDefaults (dict "registry" (dict "imagePullPolicy" "IfNotPresent")) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "eric-oss-app-lcm-stub" (dict "enabled" true)) -}}
  {{ if .Values.global }}
    {{- mergeOverwrite $globalDefaults .Values.global | toJson -}}
  {{ else }}
    {{- $globalDefaults | toJson -}}
  {{ end }}
{{ end }}


{{/*
This helper defines whether DST is enabled or not.
*/}}
{{- define "eric-oss-app-lcm.dst-enabled" }}
  {{- $dstEnabled := "false" -}}
    {{- if .Values.dst.enabled -}}
        {{- $dstEnabled = .Values.dst.enabled -}}
    {{- end -}}
  {{- $dstEnabled -}}
{{- end -}}

{{/*
Define the labels needed for DST
*/}}
{{- define "eric-oss-app-lcm.dstLabels" -}}
{{- if eq (include "eric-oss-app-lcm.dst-enabled" .) "true" }}
eric-dst-collector-access: "true"
{{- end }}
{{- end -}}

{{/*
This helper defines which exporter port must be used depending on protocol
*/}}
{{- define "eric-oss-app-lcm.exporter-port" }}
  {{- $dstExporterPort := .Values.dst.collector.portOtlpGrpc -}}
    {{- if .Values.dst.collector.protocol -}}
      {{- if eq .Values.dst.collector.protocol "http" -}}
        {{- $dstExporterPort = .Values.dst.collector.portOtlpHttp -}}
      {{- end -}}
    {{- end -}}
  {{- $dstExporterPort -}}
{{- end -}}

{{/*
Define DST environment variables
*/}}
{{ define "eric-oss-app-lcm.dstEnv" }}
{{- if eq (include "eric-oss-app-lcm.dst-enabled" .) "true" }}
- name: ERIC_TRACING_ENABLED
  value: "true"
- name: ERIC_PROPAGATOR_PRODUCE
  value: {{ .Values.dst.producer.type }}
- name: ERIC_EXPORTER_PROTOCOL
  value: '{{ .Values.dst.collector.protocol }}'
- name: ERIC_TRACING_POLING_INTERVAL
  value: '{{ .Values.dst.collector.polingInterval }}'
{{- if eq .Values.dst.collector.protocol "grpc"}}
- name: ERIC_EXPORTER_ENDPOINT
  value: {{ .Values.dst.collector.host }}:{{ include "eric-oss-app-lcm.exporter-port" . }}
{{- else if eq .Values.dst.collector.protocol "http"}}
  value: {{ .Values.dst.collector.host }}:{{ include "eric-oss-app-lcm.exporter-port" . }}/v1/traces
{{- end }}
- name: ERIC_SAMPLER_JAEGER_REMOTE_ENDPOINT
  value: {{ .Values.dst.collector.host }}:{{ .Values.dst.collector.portJaegerGrpc }}
{{- if eq .Values.dst.collector.protocol "http"}}
- name: OTEL_EXPORTER_OTLP_TRACES_PROTOCOL
  value: http/protobuf
{{- end }}
{{- else }}
- name: ERIC_TRACING_ENABLED
  value: "false"
{{- end -}}
{{ end }}

{{/*
Get acmTimeout from values or default to 1200000 (20 minutes) if not set
*/}}
{{- define "eric-oss-app-lcm.acmTimeout" -}}
  {{- $acmTimeout := .Values.config.acmTimeout | int64 -}}
  {{- if not $acmTimeout -}}
    {{- $acmTimeout = 1200000 -}}
  {{- end -}}
  {{- $acmTimeout -}}
{{- end -}}

{{/*
Template to determine whether the environment is ready to use Oauth2 based Client Credential Grant Type for inter-services communication.
If ServiceMesh and TLS and MTLS are all enabled then Client Credential Grant Type env variable is set to true.
DR-D1123-112
*/}}
{{- define "eric-oss-app-lcm.iam-client-cred-env" -}}
{{- if .Values.global }}
{{- if .Values.global.serviceMesh }}
  {{- if .Values.global.serviceMesh.enabled }}
    {{- if .Values.global.security }}
      {{- if  .Values.global.security.tls }}
        {{- if .Values.global.security.tls.enabled }}
          {{- if .Values.global.security.mTls2Iam }}
            {{- if .Values.global.security.mTls2Iam.enabled }}
                {{- "true" -}}
            {{- end }}
          {{- end }}
        {{- end }}
      {{- end }}
    {{- end }}
  {{- end }}
{{- end }}
{{- end -}}
{{- end -}}
