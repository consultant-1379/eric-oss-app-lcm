# App LCM Overview

[App LCM via ADP Marketplace](https://adp.ericsson.se/marketplace/app-lcm)

The App LCM component has the following responsibilities:

- Maintain a registry of apps deployed and instantiated with a reference to the
  artifact identities and instance identifiers as exposed by the platform services.

- Trigger instantiation/termination/deletion operations (as required) for the
  App artifacts.

The basic idea is that the App implements business logic to (partly or
completely) realize automated network management use cases. Such Apps are
intended to be small, both in terms of functionality (realizing relatively
small, well-defined use cases) and in terms of software (delegating as much
behavior as possible to the platform). The job of App LCM service is to perform
the LCM (Instantiate, upgrade (i.e. deploy new versions), terminate or delete)
of the app components.

## Main Features

### Instantiate an App

The user, requests instantiation of an app. The App LCM
determines if the user has privileges to instantiate the app, and if the app is
in enabled mode, if not, the operation is rejected. The App LCM also determines
what artifacts in the app require instantiation and requests the "owning"
service to instantiate it. The App LCM maintains a record of the instantiation
request, assigning a unique App instantiation identifier and maintains a
reference to the run-time identities of the instantiated artifacts.

### Terminate an App

The user requests Termination of an executing App, referencing
the App instantiation ID. The App LCM will determine what artifacts have been
instantiated as part of that App, and request their Termination.

### Update an App

The user, requests update of a currently instantiated app. The App
LCM determines if the user has privileges to update the app, if not, the
operation is rejected. The App LCM also determines what artifacts in the app
required for updating and requests the "owning" service to update it. The App
LCM updates all relevant instances.

### Delete an App

Instances The user requests Deletion of an instances list
referencing the App ID and their instances ID. The App LCM will determine if the
instances are in Terminated/Failed/Deleted health status and request their
physical Deletion from App Lcm and Helm Executor.

### Create IAM Client

The user requests IAM Kafka Client Creation with App Manager. The App LCM handle
IAM Client creation for rApp to access Kafka during instantiation. The user
requests IAM Kafka Client Creation with App Manager. The App LCM handle IAM
Client creation for rApp to access Kafka during instantiation.

### Delete IAM Client

The user requests IAM Kafka Client Deletion with App Manager. The App LCM
deletes the rApp IAM Kafka Client during termination. When an app is terminated
it requires that all the resources (client id, secret and scope) must be deleted
on Identity and Access Manager, and client and secret deleted from App LCM
database. The termination state marks the credentials that need to be deleted,
App LCM Job deletion will monitor those credentials.

## Contact Information

### Team

[RogerRoger Team E-mail](PDLROGERRO@pdl.internal.ericsson.com)

## Containerization and Deployment to Kubernetes cluster

Following artifacts contains information related to building a container and
enabling deployment to a Kubernetes cluster:

- [charts](charts/) folder - used by BOB to lint, package and upload helm chart
  to helm repository.

  - Once the project is built in the local workstation using the ```bob release```
    command, a packaged helm chart is available in the
    folder ```.bob/eric-oss-app-lcm-internal/``` folder. This chart can be
    manually installed in Kubernetes using ```helm install```
    command. [P.S. required only for Manual deployment from local workstation]
  - [Dockerfile](Dockerfile) - used by Spotify dockerfile maven plugin to build
    docker image.
  - The base image for the chassis application is ```sles-jdk8``` available in ```armdocker.rnd.ericsson.se```.

## Source

The [src](src/) folder of the java project contains a core spring boot
application, a controller for health check and an interceptor for helping with
logging details like user name. The folder also contains corresponding java unit
tests.

```text
src
├── main
│   ├── java
│   │   ├── com
│   │   │ └── ericsson
│   │   │     └── de
│   │   │         ├── client
│   │   │         │   └── example
│   │   │         │       └── SampleRestClient.java
│   │   │         ├── controller
│   │   │         │   ├── package-info.java
│   │   │         │   ├── example
│   │   │         │   │   ├── SampleApiControllerImpl.java
│   │   │         │   │   └── package-info.java
│   │   │         │   └── health
│   │   │         │       ├── HealthCheck.java
│   │   │         │       └── package-info.java
│   │   │         ├── CoreApplication.java
│   │   │         └── package-info.java
│   │   └── META-INF
│   │       └── MANIFEST.MF
│   └── resources
│       ├── jmx
│       │   ├── jmxremote.access
│       │   └── jmxremote.password
│       ├── v1
│       │   ├── index.html
│       │   └── microservice-chassis-openapi.yaml
│       ├── application.yaml
│       ├── logback-json.xml
│       └── bootstrap.yml
└── test
    └── java
        └── com
            └── ericsson
                └── de
                    ├── api
                    │   └── contract
                    │       ├── package-info.java
                    │       └── SampleApiBase.java
                    ├── business
                    │       └── package-info.java
                    ├── client
                    │   └── example
                    │       └── SampleRestClientTest.java
                    ├── controller
                    │   ├── example
                    │   │   ├── SampleApiControllerTest.java
                    │   │   └── package-info.java
                    │   └── health
                    │       ├── HealthCheckTest.java
                    │       └── package-info.java
                    ├── repository
                    │       └── package-info.java
                    ├── CoreApplicationTest.java
                    └── package-info.java
```

## Setting up CI Pipeline

- Docker Registry is used to store and pull Docker images. At Ericsson official
  chart repository is maintained at the org-level JFrog Artifactory. Follow the
  link to set up a [Docker registry].
- Helm repo is a location where packaged charts can be stored and shared. The
  official chart repository is maintained at the org-level JFrog Artifactory.
  Follow the link to set up a [Helm Repo].
- Follow instructions at [Jenkins Pipeline setup] to use out-of-box Jenkinsfiles
  which comes along with eric-oss-app-lcm.
- Jenkins Setup involves master and agent machines. If there is not any Jenkins
  master setup, follow instructions at [Jenkins Master] - 2.89.2 (FEM Jenkins).
- Request a node from the GIC (Note: RHEL 7 GridEngine Nodes have been
  successfully tested).
  [Request Node](https://estart.internal.ericsson.com/)
- To setup [Jenkins agent] to for Jenkins, jobs execution follow the
  instructions at Jenkins Agent Setup.
- Follow instructions at [Customize BOB] Ruleset Based on Your Project Settings
  to update ruleset files to suit to your project needs.

  [SLF4J](https://logging.apache.org/log4j/2.x/log4j-slf4j-impl/index.html)
   [Gerrit Repos](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Design+and+Development+Environment)
   [BOB](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Adopting+BOB+Into+the+MVP+Project)
   [Bob 2.0 User Guide](https://gerrit.ericsson.se/plugins/gitiles/adp-cicd/bob/+/refs/heads/master/USER_GUIDE_2.0.md)
   [Docker registry](https://confluence.lmera.ericsson.se/pages/viewpage.action?spaceKey=ACD&title=How+to+create+new+docker+repository+in+ARM+artifactory)
   [Helm repo](https://confluence.lmera.ericsson.se/display/ACD/How+to+setup+Helm+repositories+for+ADP+e2e+CICD)
   [Jenkins Master](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-JenkinsMaster-2.89.2(FEMJenkins))
   [Jenkins agent](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-Prerequisites)
   [Jenkins Pipeline setup](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-JenkinsPipelinesetup)
   [EO Common Logging](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/ESO/EO+Common+Logging+Library)
   [JFrog](https://arm.seli.gic.ericsson.se)

## Using the Helm Repo API Token

The Helm Repo API Token is usually set using credentials on a given Jenkins FEM.
If the project you are developing is part of IDUN/Aeonic this will be
pre-configured for you.
However, if you are developing an independent project please refer to the 'Helm
Repo' section:

[App LCM manages the LCM of SMO Apps comprising multiple artifacts (
microservices, models, subscriptions,
etc) [CI Pipeline Guide](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-HelmRepo)

Once the Helm Repo API Token is made available via the Jenkins job credentials
the precodereview and publish Jenkins jobs will accept the credentials (ex.
HELM_SELI_REPO_API_TOKEN' or 'HELM_SERO_REPO_API_TOKEN) and create a variable
HELM_REPO_API_TOKEN which is then used by the other files.

Credentials refers to a user or a functional user. This user may have access to
multiple Helm repos.
In the event where you want to change to a different Helm repo, that requires a
different access rights, you will need to update the set credentials.

## Artifactory Set-up Explanation

The App LCM manages the LCM of SMO Apps comprising multiple artifacts (
microservices, models, subscriptions, etc) Artifactory repos (dev, ci-internal
and drop) are set up following the [ADP
principles](https://confluence.lmera.ericsson.se/pages/viewpage.action?spaceKey=AA&title=2+Repositories)

The commands: "bob init-dev build image package" will ensure that you are
pushing a Docker image to:
[Docker registry - Dev](https://arm.seli.gic.ericsson.se/artifactory/docker-v2-global-local/proj-eric-oss-dev/)

The Precodereview Jenkins job pushes a Docker image to:
[Docker registry - CI Internal](https://arm.seli.gic.ericsson.se/artifactory/docker-v2-global-local/proj-eric-oss-ci-internal/)

This is intended behaviour which mimics the behavior of the Publish Jenkins job.
This job presents what will happen when the real microservice image is being
pushed to the drop repository.
Furthermore, the 'Helm Install' stage needs a Docker image which has been
previously uploaded to a remote repository, hence why making a push to the CI
Internal is necessary.

The Publish job also pushes to the CI-Internal repository, however the Publish
stage promotes the Docker image and Helm chart to the drop repo:
[Docker registry - Drop](https://arm.seli.gic.ericsson.se/artifactory/docker-v2-global-local/proj-eric-oss-drop/)

Similarly, the Helm chart is being pushed to three separate repositories:
[Helm registry - Dev](https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-dev-helm/)

The Precodereview Jenkins job pushes the Helm chart to:
[Helm registry - CI Internal](https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-ci-internal-helm/)

This is intended behaviour which mimics the behavior of the Publish Jenkins job.
This job presents what will happen when the real Helm chart is being pushed to
the drop repository.
The Publish Jenkins job pushes the Helm chart to:
[Helm registry - Drop](https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm/)
