
# App Lcm PRI

APR 201 534, R1A
App Lcm

## Revision History

| Revision | Date       | Reason for Revision                                     | Reviewer               |
|----------|------------|---------------------------------------------------------|------------------------|
| PA1      | 2022-01-10 | First draft                                             | Andres Leal            |
| PA2      | 2022-02-10 | Maturity Level increase and fixes                       | Andres Leal            |
| PA3      | 2022-04-05 | App Manager: Enabling and disabling apps                | Kiruthiga Muralidharan |
| PA4      | 2022-05-16 | Terminate use case: Remove Deletion of WorkloadInstance | Kiruthiga Muralidharan |
| PA5      | 2022-06-29 | App-Lcm: Delete app Instances                           | Sharon Keane           |
| PA6      | 2022-06-30 | App-Lcm: Delete Apps                                    | Sharon Keane           |
| PA7      | 2024-03-20 | Update App Onboarding and App LCM to use ACM-R          | Harish Kumar           |

## Reason for Revision

PA5: As part of app deletion instances must be deleted from app lcm
so that we clean up space and resources that are not going to be used.

PA6: As Authorized Admin user I want to delete an app
so that all its artefacts and instances get deleted from the system.

PA7: As Authorized Admin user I want to Update App Onboarding and App
LCM to use ACM-R. 
Epic link to step:
https://eteamproject.internal.ericsson.com/browse/IDUN-19684

## Evidence of Conformity with the Acceptance Criteria

The release criteria have been fulfilled.

The release decision has been taken by the approval of this document.

## Technical Solution

For IDUN-25456 App Lcm: Delete Apps, integration with app OnBoarding
code changes has been necessary - IDUN-28413

### Implemented Requirements

| REQUIREMENT ID (MR/JIRA ID) | HEADING/DESCRIPTION                                     |
|-----------------------------|---------------------------------------------------------|
| IDUN-1945                   | Introduce App LCM Service                               |
| IDUN-9804                   | App LCM: Service Maturity Ready for Non-Commercial Use  |
| IDUN-14411                  | App LCM: Instantiate based on enable/disable state      |
| IDUN-23513                  | Terminate use case: Remove Deletion of WorkloadInstance |
| IDUN-15785                  | App Lcm: Delete app Instances                           |
| IDUN-25456                  | App Lcm: Delete Apps                                    |
| IDUN-48920                  | Introduce flyway                                        |

### Implemented additional features

No additional features implemented in this release

| JIRA-ID          | JIRA HEADING/DESCRIPTION                                           |
|------------------|--------------------------------------------------------------------|
| IDUN-24139       | Update GET All App instances with Filter Criteria                  |

### Implemented API Changes

No API changes for this last change

### SW Library

No SW library product for this service.

| SW LIBRARY NAME | PRODUCT NUMBER | OLDEST COMPATIBLE VERSION |
|-----------------|----------------|---------------------------|
|                 |                |                           |

### Reusable Images

No reusable images products for this service.

### Impact on Users: Abrupt NBC

No Abrupt NBC introduced in this release.

### Corrected Vulnerability Trouble Reports

No vulnerability trouble reports fixed in this release.

| Vulnerability ID(s) | Vulnerability Description                                                          | TR ID       |
|---------------------|------------------------------------------------------------------------------------|-------------|
| CVE-2024-1597       | Risk of SQL injection                                                              | IDUN-113506 |
| CVE-2023-6378       | Risk of Denial of Service (DOS) when decompressing data                            | IDUN-115209 |
| CVE-2023-6378       | Risk of Denial of Service (DOS) Denial-Of-Service attack by sending poisoned data. | IDUN-115191 |
| CVE-2023-43642      | Risk of Denial of Service (DOS) when decompressing data                            | IDUN-115218 |
| CVE-2023-33201      | Risk of injection vulnerability                                                    | IDUN-115221 |
| CVE-2023-42503      | Risk of Denial of Service (DOS) when parsing tar                                   | IDUN-115225 |

### Restrictions and Limitations

As for today, only one instance can be created for the same app and the same
namespace in App-Lcm. It means that you can provide list of instances to delete,
but physically only one instance can be deleted.

#### Exemptions

No exemption is present in this release.

#### Open Trouble Reports

No open Trouble Reports

| TR ID | TR HEADING | Priority |
|-------|------------|----------|
|       |            |          |

#### Backward incompatibilities

No NBC introduced in this release.

#### Unsupported Upgrade/Rollback paths

No NUC/NRC introduced in this release.

| Oldest version for which upgrade to this release is supported | Oldest version for which rollback from this release is supported |
|---------------------------------------------------------------|------------------------------------------------------------------|
|                                                               |                                                                  |

#### Features not ready for commercial use

No Feature with Feature Maturity Alpha/Beta included in this release

## Product Deliverables

### Software Products

The following table shows the software products of this release.

| Product Type | Name | Product ID | New R-state | SHA256 Checksum |
|--------------|------|------------|-------------|-----------------|
|              |      |            |             |                 |

### New and Updated 2PP/3PP

(This section shall report all new and updated 2PPs and 3PPs integrated by the service.
This includes SW libraries and reusable images embedded as 2PP in the service.)

There is no new 2PP/3PP's as part of current revision.

The following is existing 2PP/3PP’s:

| Name                      | Product ID    | Old Version | New Version   |
|---------------------------|---------------|-------------|---------------|
| jackson-databind-nullable | 6/CTX1027927  | 0.2.4       | 0.2.6         |
| jaeger-client-java        | 8/CTX1022999  | ---         | RELEASE-1.6.0 |
| swagger-annotations       | 11/CAX1056693 | ---         | 1.6.2         |
| PostgresSQL JDBC Driver   | 70/CAX1053319 | 42.4.1      | 42.5.1        |
| Lombok                    | 14/CAX1056250 | ---         | 1.18.16       |
| H2 Database Engine        | 27/CAX1054380 | 1.4.200     | 2.1.210       |
| Micrometer                | 64/CTX1023759 | 1.8.5       | 1.9.0         |
| Spring Cloud              | 20/CTX1020469 | 2021.0.1    | 2021.0.3      |
| logstash-logback-encoder  | 7/CTX1020602  | ---         | 6.5           |
| openjdk                   | 8/CTX1025426  | ---         | 11            |
| SpringFox                 | 12/CAX1058435 | ---         | 3.0.0         |
| springdoc-openapi-ui      | 6/CTX1027933  | ---         | 1.5.8         |
| orika-core                | 3/CAX1058185  | ---         | 1.5.2         |
| commons-io                | 20/CAX1053602 | 2.6         | 2.11.0        |
| commons-beanutils         | 11/CAX1053285 | ---         | 1.9.4         |
| commons-collections       | 9/CAX1053447  | ---         | 3.2.2         |
| ClassGraph                | 17/CTX1024906 | ---         | 4.8.138       |
| SnakeYAML                 | 27/CAX1056807 | 1.33        | 2.0           |
| Flyway                    | 34/CAX1056970 | ---         | 8.5.2         |
| Spring Boot, Spring       | 88/CAX1058168 | 2.4.2       | 2.7.10        |
| Freemarker                | 20/CAX1053187 | ---         | 2.2.32        |
| Modelmapper               | 15/CAX1059997 | ---         | 3.1.0         |

### Helm Chart Link

The following table shows the repository manager links for this release:

| RELEASE     | HELM PACKAGE LINK                                                                                                      |
|-------------|------------------------------------------------------------------------------------------------------------------------|
| App-Lcm R1A | <https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm/eric-oss-app-lcm/eric-oss-app-lcm-1.0.129-1.tgz> |

### Related Documents

All the documents can be accessible from Marketplace, see chapter Product
Documentation for details.

## Product Documentation

### Developer Product Information

The Developer Product Information (DPI) documentation can be accessed using the
following link:
<https://adp.ericsson.se/marketplace/app-lcm>

### Customer Product Information

This service does not provide any CPI content.

## Deployment Information

The App Lcm can be deployed in a Kubernetes environment.

### Deployment Instructions

The target group for the deployment instructions is only application developers
and application integrators.
Deployment instruction can be found in the [App Lcm User Guide][userguide].

### Upgrade Information

This is a new product.

## Verification Status

The verification status will be described in the App Lcm/ App manager Test Report.

### Stakeholder Verification

It is verified in ADP CICD pipeline as part of the ADP staging phase and in
additional to application staging pipelines. See CI/CD Dashboard for further
understanding.
Note: that result showing in this CI/CD Dashboard pipeline is always result of
the latest build and is not this specific PRA release.

## Support

For support use the Generic Services Support JIRA project, please also see the
Service Name Service troubleshooting guidelines in Marketplace where you will
find more detailed support information.

## References

[JIRA][jira]

[ADP Marketplace][marketplace]

[User Guide][userguide]

[jira]:<https://jira-oss.seli.wh.rnd.internal.ericsson.com/secure/RapidBoard.jspa?rapidView=7787&view=reporting&chart=sprintRetrospective&sprint=32429>
[marketplace]:<https://adp.ericsson.se/marketplace/app-lcm>
[userguide]:<https://adp.ericsson.se/marketplace/app-lcm/documentation/1.0.0/dpi/service-user-guide>
