# App LCM Developers Guide

This information includes developer-related topics for using App LCM.
It includes these sections:

[TOC]

## Introduction
This document Provides guidelines for how to use the

App LCM Service from an application developer’s point of view.

It gives a brief description of its main features and its interfaces.

## V1 Interfaces

### Instantiate an instance of an App

Path: POST ```/app-lcm/v1/app-instances```

Header: Content-Type:application/json

Body:

```json
{
  "appId": 456,
  "additionalParameters": {
  }
}
```

Body Attributes:

| Name                 | Type   | Mandatory | Description                                |
|----------------------|--------|-----------|--------------------------------------------|
| appId                | Number | Yes       | On boarding App ID                         |
| additionalParameters | Object | No        | Optional parameters to instantiate an App. |

**Additional parameters keys:**

| Name                 | Type          | Required | Description                                                                |
|----------------------|---------------|----------|----------------------------------------------------------------------------|
| workloadInstanceId   | String        | No       | Identifier of the workloadInstance.                                        |
| workloadInstanceName | String        | No       | Name of the workloadInstance.                                              |
| namespace            | String        | No       | Namespace where workloadInstance is installed.                             |
| crdNamespace         | String        | No       | Namespace where Custom Resource Definition (CRD) characters are installed. |
| cluster              | String        | No       | Cluster name where the workloadInstance is instantiated.                   |
| versions             | Array[String] | No       | Identifies versions of helmSources used for lifecycle operations.          |

Additional parameters can also be used to override any parameters declared in an app microservice helm charts, specifically in the values.yaml file.

**Additional parameters keys for Persistent Volumes (PV):**

| Name                 | Type          | Required | Description                                                                |
|----------------------|---------------|----------|----------------------------------------------------------------------------|
| replicaCount         | Int           | No       | The number of replicas (pods) the controller should always be running.     |
| storage              | String        | No       | Identifies a specific storage capacity for Persistent Volumes.             |
| volumeMode           | String        | No       | Identifies volume modes of Persistent Volumes: Filesystem or Block.        |

For such helm values, these are examples of additionalParameters that can be configured for PV.

Example:

```json
{
  "id": 1,
  "appOnBoardingAppId": 456,
  "healthStatus": "PENDING",
  "targetStatus": "INSTANTIATED",
  "createdTimestamp": "2021-08-19 19:10:25-07",
  "links": [
    {
      "rel": "self",
      "href": "app/manger/lcm/app-lcm/v1/app-instance/1"
    },
    {
      "rel": "artifact-instances",
      "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances"
    },
    {
      "rel": "app",
      "href": "app/manager/onboarding/v1/apps/456"
    },
    {
      "rel": "artifacts",
      "href": "app/manager/onboarding/v1/apps/456/artifacts"
    }
  ]
}
```

Responses:
This API call produces the following media types according to the
accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description                                                                           |
|------------------|---------------------------------------------------------------------------------------|
|  201 - Created   | The API producer responds with this response code when a successful operation occurs. |

More details about common errors can be found in the
[Error data structure](#error-data-structure) section.

### Update an instance of an App

Path: PUT ```/app-lcm/v1/app-instances/```

Header: Content-Type:application/json

Body:

```json
{
  "appInstanceId": 1,
  "appOnBoardingAppId": 789,
  "additionalParameters": {
  }
}
```

Body Attributes:

| Name                 | Type   | Mandatory | Description                             |
|----------------------|--------|-----------|-----------------------------------------|
| appInstanceId        | Number | Yes       | App-Lcm App ID.                         |
| appOnBoardingAppId   | Number | Yes       | Onboarding App ID for new app(update).  |
| additionalParameters | Object | No        | Optional parameters to update an App.   |

Additional parameters keys:

Additional parameter keys are supported for both single and intgeration charts. The values are updated in the database.

Example:

```json
{
  "id": 1,
  "appOnBoardingAppId": 789,
  "healthStatus": "PENDING",
  "targetStatus": "INSTANTIATED",
  "createdTimestamp": "2021-08-19 19:10:25-07",
  "links": [
    {
      "rel": "self",
      "href": "app/manger/lcm/app-lcm/v1/app-instance/1"
    },
    {
      "rel": "artifact-instances",
      "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances"
    },
    {
      "rel": "app",
      "href": "app/manager/onboarding/v1/apps/456"
    },
    {
      "rel": "artifacts",
      "href": "app/manager/onboarding/v1/apps/456/artifacts"
    }
  ]
}
```

Responses:
This API call produces the following media types according to the
accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description                                                                           |
|------------------|---------------------------------------------------------------------------------------|
|  200 - OK        | The API producer responds with this response code when a successful operation occurs. |

More details about common errors can be found in the
[Error data structure](#error-data-structure) section.

### Terminate an Instance of an App

Path: PUT```/app-lcm/v1/app-instances/{appInstanceId}```

Header: Content-Type:application/json

Parameters:

| Name           | Type   | Required | Description                                  |
|----------------|--------|----------|----------------------------------------------|
|  appInstanceId | Number | Yes      | The ID of the app instance to be terminated. |

Example:

PUT ```/app-lcm/v1/app-instances/1```

Responses: This API call produces the following media types according to the
accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code  | Description                                                      | Response Data Structure |
|-------------------|------------------------------------------------------------------|-------------------------|
|  204 - No Content | A successful operation, which does not return a response entity. |                         |

More details about common errors can be found in the
[Error data structure](#error-data-structure) section.

### Delete a List of Instances for the App

DELETE ```/app-lcm/v1/apps/{appId}/app-instances```

Header: Content-Type:application/json

Parameters:

| Name   | Type   | Required | Description                                                     |
|--------|--------|----------|-----------------------------------------------------------------|
| appId  | Number | Yes      | The ID of the app for the instance(s) designated to be deleted. |

Body:

```json
{
  "appInstanceId": [1,2,3,4,5]
}
```

Body Attributes:

| Name                 | Type   | Mandatory | Description              |
|----------------------|--------|-----------|--------------------------|
| appInstanceId        | Number | Yes       | App-Lcm App Instance ID. |

Example:

DELETE ```/app-lcm/v1/apps/1/app-instances```

```json
{
  "appInstanceId": [1,2,3,4,5]
}
```

Responses: This API call produces the following media types according to the
accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code  | Description                                                      | Response Data Structure |
|-------------------|------------------------------------------------------------------|-------------------------|
|  204 - No Content | A successful operation, which does not return a response entity. |                         |

More details about common errors can be found in the
[Delete-Api Error data structure](#delete-api-error-data-structure) section.

### Delete an App

DELETE ```/app-lcm/v1/apps/{appId}```

Header: Content-Type:application/json

Parameters:

| Name   | Type   | Required | Description                      |
|--------|--------|----------|----------------------------------|
| appId  | Number | Yes      | The ID of the app to be deleted. |

Example:

DELETE ```/app-lcm/v1/apps/1/```

Responses: This API call produces the following media types according to the
accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description                                                                                                                                  | Response Data Structure |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|
| 202 - Accepted   | Operation accepted, which means that the request has been accepted for processing, but the processing has not been completed - asynchronous. |                         |

Delete App API accepts the request and if the request is valid all the
instances for the app are waiting for monitoring job to be deleted.
Monitoring job runs every 30 seconds and pick up instances designated
to delete and delete them physically.
As for now, The only way to check if the app has been deleted, is to call
OnBoarding API to GET deleted app.

To check if the designated to delete app has been Deleted
(Expect 404 NOT FOUND) use App OnBoarding API:

GET ```/v1/apps/1```

More details about common errors can be found in the
[Delete-Api Error data structure](#delete-api-error-data-structure) section.

### Get a List of All or specific Instances of every App

Path: GET ```/app-lcm/v1/app-instances```

Params: required: false    appId:12L

Header: Content-Type:application/json

Example:

GET ```/app-lcm/v1/app-instances```

```json
[
  {
    "id": 1,
    "appOnBoardingAppId": 234,
    "healthStatus": "FAILED",
    "targetStatus": "INSTANTIATED",
    "createdTimestamp": "2021-08-19 19:10:25-07",
    "links": [
      {
        "rel": "self",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/1"
      },
      {
        "rel": "artifact-instances",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances"
      },
      {
        "rel": "app",
        "href": "app/manager/onboarding/v1/apps/234"
      },
      {
        "rel": "artifacts",
        "href": "app/manager/onboarding/v1/apps/234/artifacts"
      }
    ]
  },
  {
    "id": 2,
    "appOnBoardingAppId": 235,
    "healthStatus": "PENDING",
    "targetStatus": "TERMINATED",
    "createdTimestamp": "2021-08-19 19:10:25-07",
    "links": [
      {
        "rel": "self",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/2"
      },
      {
        "rel": "artifact-instances",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/2/artifact-instances"
      },
      {
        "rel": "app",
        "href": "app/manager/onboarding/v1/apps/235"
      },
      {
        "rel": "artifacts",
        "href": "app/manager/onboarding/v1/apps/235/artifacts"
      }
    ]
  }
]
```

GET ```/app-lcm/v1/app-instances/?appId=12```

```json
[
  {
    "id": 1,
    "appOnBoardingAppId": 12,
    "healthStatus": "FAILED",
    "targetStatus": "INSTANTIATED",
    "createdTimestamp": "2021-08-19 19:10:25-07",
    "links": [
      {
        "rel": "self",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/1"
      },
      {
        "rel": "artifact-instances",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances"
      },
      {
        "rel": "app",
        "href": "app/manager/onboarding/v1/apps/234"
      },
      {
        "rel": "artifacts",
        "href": "app/manager/onboarding/v1/apps/234/artifacts"
      }
    ]
  },
  {
    "id": 2,
    "appOnBoardingAppId": 12,
    "healthStatus": "PENDING",
    "targetStatus": "TERMINATED",
    "createdTimestamp": "2021-08-19 19:10:25-07",
    "links": [
      {
        "rel": "self",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/2"
      },
      {
        "rel": "artifact-instances",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/2/artifact-instances"
      },
      {
        "rel": "app",
        "href": "app/manager/onboarding/v1/apps/235"
      },
      {
        "rel": "artifacts",
        "href": "app/manager/onboarding/v1/apps/235/artifacts"
      }
    ]
  }
]
```

Responses: This API call produces the following media types according to the
accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description                                              |
|------------------|----------------------------------------------------------|
|  200 - OK        | A successful operation, which returns a response entity. |

More details about common errors can be found in the
[Error data structure](#error-data-structure) section.

### Get a Specific Instance of an App

Path: GET ```/app-lcm/v1/app-instances/{appInstanceId}```

Header: Content-Type:application/json

Parameters:

| Name           | Type   | Required | Description                 |
|----------------|--------|----------|-----------------------------|
|  appInstanceId | Number | Yes      | The ID of the App instance. |

Example:

GET ```/app-lcm/v1/app-instances/1```

```json
{
  "id": 1,
  "appOnBoardingAppId": 234,
  "healthStatus": "INSTANTIATED",
  "targetStatus": "INSTANTIATED",
  "createdTimestamp": "2021-08-19 19:10:25-07",
  "links": [
    {
      "rel": "self",
      "href": "app/manger/lcm/app-lcm/v1/app-instance/1/"
    },
    {
      "rel": "artifact-instances",
      "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances"
    },
    {
      "rel": "app",
      "href": "app/manager/onboarding/v1/apps/234"
    },
    {
      "rel": "artifacts",
      "href": "app/manager/onboarding/v1/apps/234/artifacts"
    }
  ]
}
```

Responses: This API call produces the following media types according to
the accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description                                              |
|------------------|----------------------------------------------------------|
|  200 - OK        | A successful operation, which returns a response entity. |

More details about common errors can be found in the
[Error data structure](#error-data-structure) section.

### To Get a List of All Artifacts From an Instance

Path: GET ```/app-lcm/v1/app-instances/{appInstanceId}/artifact-instances```

Header: Content-Type:application/json

Parameters:

| Name           | Type   | Required | Description                 |
|----------------|--------|----------|-----------------------------|
|  appInstanceId | Number | Yes      | The ID of the App instance. |

Example:

GET ```/app-lcm/v1/app-instances/1/artifact-instances```

```json
[
  {
    "artifactInstanceId": 1,
    "artifactId": 123,
    "healthStatus": "PENDING",
    "createdTimestamp": "2021-08-19 19:10:25-07",
    "statusMessage": null,
    "links": [
      {
        "rel": "self",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances/1"
      },
      {
        "rel": "artifact-instances",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances"
      },
      {
        "rel": "app-instance",
        "href": "app/manger/lcm/app-lcm/v1/app-instances/1"
      },
      {
        "rel": "app",
        "href": "app/manager/onboarding/v1/apps/456"
      },
      {
        "rel": "artifact",
        "href": "app/manager/onboarding/v1/apps/456/artifacts/123"
      }
    ]
  },
  {
    "artifactInstanceId": 2,
    "artifactId": 124,
    "healthStatus": "FAILED",
    "createdTimestamp": "2021-08-19 19:10:25-07",
    "statusMessage": null,
    "links": [
      {
        "rel": "self",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances/2"
      },
      {
        "rel": "artifact-instances",
        "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances"
      },
      {
        "rel": "app-instance",
        "href": "app/manger/lcm/app-lcm/v1/app-instances/1"
      },
      {
        "rel": "app",
        "href": "app/manager/onboarding/v1/apps/456"
      },
      {
        "rel": "artifact",
        "href": "app/manager/onboarding/v1/apps/456/artifacts/124"
      }
    ]
  }
]
```

Responses: This API call produces the following media types according to
the accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description                                              |
|------------------|----------------------------------------------------------|
|  200 - OK        | A successful operation, which returns a response entity. |

More details about common errors can be found in the
[Error data structure](#error-data-structure) section.

### Get a Specific Artifact from an Instance

Path: GET ```/app-lcm/v1/app-instances/{appInstanceId}/artifact-instances/{artifactInstanceId}```

Header: Content-Type:application/json

Parameters:

| Name                | Type   | Required | Description                 |
|---------------------|--------|----------|-----------------------------|
| appInstanceId       | Number | Yes      | The ID of the App instance. |
|  artifactInstanceId | Number | Yes      | The ID of artifact instance. |

Example:

GET ```/app-lcm/v1/app-instances/451/artifact-instances/1```

```json
{
  "artifactInstanceId": 1,
  "artifactId": 123,
  "healthStatus": "PENDING",
  "createdTimestamp": "2021-08-19 19:10:25-07",
  "links": [
    {
      "rel": "self",
      "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances/1"
    },
    {
      "rel": "artifact-instances",
      "href": "app/manger/lcm/app-lcm/v1/app-instance/1/artifact-instances"
    },
    {
      "rel": "app-instance",
      "href": "app/manger/lcm/app-lcm/v1/app-instances/1"
    },
    {
      "rel": "app",
      "href": "app/manager/onboarding/v1/apps/456"
    },
    {
      "rel": "artifact",
      "href": "app/manager/onboarding/v1/apps/456/artifact/123"
    }
  ]
}
```

Responses: This API call produces the following media types according to
the accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description                                              |
|------------------|----------------------------------------------------------|
|  200 - OK        | A successful operation, which returns a response entity. |

More details about common errors can be found in the
[Error data structure](#error-data-structure) section.

### Error data structure

Common error responses used by all APIs.

| HTTP Status Code             | Description                                                                                                                                                                                                  |
|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 400 - Bad Request            | The API producer responds with this response code if, for example, the request URI contains incorrect query parameters or if the payload body contains a syntactically incorrect data structure.             |
| 401 - Unauthorized           | The API producer responds with this response code when the request does not contain an access token even though it is required, or if the request contains an authorization token that is invalid.           |
| 404 - Not Found              | The API producer responds with this response code if it did not find a current representation for the resource addressed by the URI passed in the request, or it is not willing to disclose that one exists. |
|  500 - Internal Server Error | The API producer responds with this response code if there is an application error not related to the clients input that cannot be easily mapped to any other HTTP response code.                            |

Example

```json
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "app-lcm-error-code": 0,
  "app-lcm-error-message": "string",
  "url": "string"
}
```

### Delete Api Error data structure

Error responses used by Delete APIs.

| HTTP Status Code            | Description                                                                                                                                                                                                  |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 200 - OK                    | The API producer responds with this response code if deletion was unsuccessful or partly successful. It will provide information about each unsuccessful instance and total number of successful deletion.   |
| 401 - Unauthorized          | The API producer responds with this response code when the request does not contain an access token even though it is required, or if the request contains an authorization token that is invalid.           |
| 404 - Not Found             | The API producer responds with this response code if it did not find a current representation for the resource addressed by the URI passed in the request, or it is not willing to disclose that one exists. |
| 500 - Internal Server Error | The API producer responds with this response code if there is an application error not related to the clients input that cannot be easily mapped to any other HTTP response code.                            |

Examples

200: unsuccessful

```json
{
  "appLcmErrorMessage": "App Lcm failed to delete",
  "appLcmErrorCode": 1020,
  "totalSuccessful": 0,
  "errorData": [
    {
      "appInstanceId": 10,
      "failureMessage": "Could not find the app instance specified.",
      "appLcmErrorCode": 1001
    },
    {
      "appInstanceId": 11,
      "failureMessage": "Requested artifact instance is not in TERMINATE or FAILED state.",
      "appLcmErrorCode": 1030
    },
    {
      "appInstanceId": 12,
      "failureMessage": "Could not find the app instance specified.",
      "appLcmErrorCode": 1001
    }
  ]
}
```

200: partially succeeded

```json
{
  "appLcmErrorMessage": "The multiple delete request has partially succeeded.",
  "appLcmErrorCode": 1028,
  "totalSuccessful": 1,
  "errorData": [
    {
      "appInstanceId": 2,
      "failureMessage": "Could not find the app instance specified.",
      "appLcmErrorCode": 1001
    },
    {
      "appInstanceId": 3,
      "failureMessage": "Could not find the app instance specified.",
      "appLcmErrorCode": 1001
    }
  ]
}
```

404:

```json
{
  "type": "Not Found",
  "title": "Not Found",
  "status": 404,
  "detail": "No App Instances were found for appId: 2",
  "appLcmErrorCode": 1001,
  "appLcmErrorMessage": "Could not find the app instance specified.",
  "url": "/app-lcm/v1/apps/2"
}
```
## V3 App Interfaces

### Create App
Summary: This is an internal interface used by the App Onboarding service as the final stage of onboarding an App to the system. The request body is created internally in App Onboarding service based on the contents of the App Package.

Path: POST _/apps_

Header: Content-Type:application/json

Example Body:

POST /app-lifecycle-management/v3/apps

````json
{
  "name": "eric-oss-hello-world-multiple-microservices-go-app",
  "version": "1.1.1",
  "type": "rApp",
  "components": [
    {
      "type": "Microservice",
      "name": "eric-oss-hello-metrics-go-app",
      "version": "1.1.1",
      "artifacts": [
        {
          "type": "HELM",
          "name": "eric-oss-hello-world-go-app",
          "location": "26471a81-1de4-4ad9-9724-326eefd22230/eric-oss-hello-world-go-app"
        },
        {
          "type": "IMAGE",
          "name": "docker.tar",
          "location": "26471a81-1de4-4ad9-9724-326eefd22230/docker"
        }
      ]
    }
  ],
  "permissions": [
    {
      "resource": "BDR",
      "scope": "bdr-policy"
    }
  ],
  "roles": [
    {
      "name": "admin"
    }
  ]
}
````

Body parameters:

| Name        | Type   | Description                                                                   |
|-------------|--------|-------------------------------------------------------------------------------|
| name        | String | Name of App                                                                   |
| version     | String | Version of App                                                                |
| type        | String | Type of App                                                                   |
| components  | array  | An array of {AppComponent} of the App                                         |
| permissions | array  | An array of {AppPermission} of the App                                        |
| roles       | array  | An array of (AppRole) RBAC roles required by the App to consume external APIs |

App Component:

| Name      | Type   | Description                                  |
|-----------|--------|----------------------------------------------|
| name      | String | The name of the App component                |
| type      | String | The type of the App component                |
| version   | String | The version of the App component             |
| artifacts | array  | The array of {artifact} of the App component |

Artifact:

| Name     | Type   | Description                                            |
|----------|--------|--------------------------------------------------------|
| name     | String | The name of the artifact                               |
| type     | String | The type of the artifact                               |
| location | String | The location of the artifact in the object store minio |

AppPermission:

| Name     | Type   | Description                        |
|----------|--------|------------------------------------|
| resource | String | The resource of the App permission |
| scope    | String | The scope of the App permission    |

AppRole

| Name | Type   | Description          |
|------|--------|----------------------|
| name | String | The name of the role |

Responses: This API call produces the following media types according to the accepted request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description        |
|------------------|--------------------|
| 201 - Created    | Create App Request |

More details about common errors can be found in the [Error data structure](#error-data-structure) section.

### Get All Apps

Summary: This interface retrieves all the information of all Apps that were created in the system.

Path: GET _/app-lifecycle-management/v3/apps_

Header: Content-Type:application/json

Example:

GET /app-lifecycle-management/v3/apps?name=eric-oss-hello-world-multiple-microservices-go-app

Body parameters: N/A

URI parameters:

| Name    | Type   | Description                                                                  |
|---------|--------|------------------------------------------------------------------------------|
| name    | String | Filter by App name                                                           |
| version | String | Filter by App version                                                        |
| mode    | String | Filter by App mode                                                           |
| status  | String | Filter by App status                                                         |
| type    | String | Filter by App type                                                           |
| offset  | string | The offset specifies the start element for the jobs returned                 |
| limit   | String | Limit is used with the offset query param to return a paginated list of apps |

Response: This API call produces the following media types according to the OK request header with the App information included in the response body.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description          |
|------------------|----------------------|
| 200 - OK         | OK Get Apps response |

Response body example:

````json
{
  "items": [
    {
      "id": "26471a81-1de4-4ad9-9724-326eefd22230",
      "name": "eric-oss-hello-world-multiple-microservices-go-app",
      "version": "1.1.1",
      "mode": "DISABLED",
      "status": "CREATED",
      "createdAt": "2023-04-12T18:06:57.886+00:00",
      "type": "rApp",
      "components": [
        {
          "type": "Microservice",
          "name": "eric-oss-hello-metrics-go-app",
          "version": "1.1.1",
          "artifacts": [
            {
              "type": "HELM",
              "name": "eric-oss-hello-world-go-app",
              "location": "26471a81-1de4-4ad9-9724-326eefd22230/eric-oss-hello-world-go-app"
            },
            {
              "type": "IMAGE",
              "name": "docker.tar",
              "location": "26471a81-1de4-4ad9-9724-326eefd22230/docker"
            }
          ]
        }
      ],
      "permissions": [
        {
          "resource": "BDR",
          "scope": "bdr-policy"
        }
      ],
      "roles": [
        {
          "name": "admin"
        }
      ],
      "events": [],
      "self": {
        "href": "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230"
      }
    }
  ]
}
````

### Get Specific App

Summary: This interface retrieves all the information of a specific App that was created in the system.

Path: GET _/app-lifecycle-management/v3/apps/{appId}_

Header: Content-Type:application/json

Example:

GET /app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230

Header parameter:

| Name  | Type   |
|-------|--------|
| appId | String |

Response: This API call produces the following media types according to the OK request header with the App information included in the response body.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description          |
|------------------|----------------------|
| 200 - OK         | OK Get Apps response |

Response Example:

````json
{
  "items": [
    {
      "id": "26471a81-1de4-4ad9-9724-326eefd22230",
      "name": "eric-oss-hello-world-multiple-microservices-go-app",
      "version": "1.1.1",
      "mode": "DISABLED",
      "status": "CREATED",
      "createdAt": "2023-04-12T18:06:57.886+00:00",
      "type": "rApp",
      "components": [
        {
          "type": "Microservice",
          "name": "eric-oss-hello-metrics-go-app",
          "version": "1.1.1",
          "artifacts": [
            {
              "type": "HELM",
              "name": "eric-oss-hello-world-go-app",
              "location": "26471a81-1de4-4ad9-9724-326eefd22230/eric-oss-hello-world-go-app"
            },
            {
              "type": "IMAGE",
              "name": "docker.tar",
              "location": "26471a81-1de4-4ad9-9724-326eefd22230/docker"
            }
          ]
        }
      ],
      "permissions": [
        {
          "resource": "BDR",
          "scope": "bdr-policy"
        }
      ],
      "roles": [
        {
          "name": "admin"
        }
      ],
      "events": [],
      "self": {
        "href": "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230"
      }
    }
  ]
}
````

### Initialize/De-initialize App

Summary: This interface prepares and validates the App schema in the system. It is a post-step for App onboarding and a pre-step for App instantiation. When deinitialize action is requested the App schema is cleaned up internally. This is a pre-step to App deletion.

Path: POST /app-lifecycle-management/v3/apps/{appId}/initialization-actions

Header: Content-Type:application/json

Example:

POST /app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230/initialization-actions

Header parameter:

| Name  | Type   |
|-------|--------|
| appId | String |

Example request body:

````json
{
  "action": "INITIALIZE"
}
````

Body parameter:

| Name   | Type   | Options                       |
|--------|--------|-------------------------------|
| action | String | - INITIALIZE<br>- DEINTIALIZE |

Response: This API call produces the following media types according to the ACCEPTED request header with the App information included in the response body.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description |
|------------------|-------------|
| 202              | Accepted    |

Response example:

````json
{
  "app": {
    "id": "26471a81-1de4-4ad9-9724-326eefd22230",
    "status": "INITIALIZING",
    "href": "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230"
  }
}
````

### Enabling/Disabling an App
Summary: This interface is an administrative action, when enabled the App is available for instantiation.

Path: PUT /app-lifecycle-management/v3/apps/{appId}/mode

Header: Content-Type:application/json

Example:

PUT /app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230/mode

Header parameters:

| Name  | Type   |
|-------|--------|
| appId | String |

Body example:

````json
{
  "mode": "ENABLED"
}
````

Body parameter:

| Name | Type   | Options                  |
|------|--------|--------------------------|
| mode | String | - ENABLED<br/>- DISABLED |

Response: This API call produces the following media types according to the OK request header with some of the App information and the type of mode chosen in the response body.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description |
|------------------|-------------|
| 200              | OK          |

Response body example:

````json
{
  "mode": "ENABLED",
  "app": {
    "id": "26471a81-1de4-4ad9-9724-326eefd22230",
    "href": "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230"
  }
}
````

### Delete an App

Summary: This interface removes the App in its entirety from the system. An App must be deinitialized and disabled prior to deletion.

Path: DELETE /app-lifecycle-management/v3/apps/{appId}

Header: Content-Type:application/json

Example:

DELETE /app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230

Header parameters:

| Name  | Type   |
|-------|--------|
| appId | String |

Body parameter: N/A

Response: This API call produces the following media types according to the NO CONTENT request header.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description |
|------------------|-------------|
| 204              | No Content  |

## V3 App Instance Interfaces

### Create App Instance
Summary: This interface creates an App instance. The App must be initialized and enabled prior to creation.

Path: POST /app-lifecycle-management/v3/app-instances

Header: Content-Type:application/json

Example:

POST /app-lifecycle-management/v3/app-instances

Body Example:

````json
{
  "appId": "26471a81-1de4-4ad9-9724-326eefd22230"
}
````

Body parameters:

| Name  | Type   |
|-------|--------|
| appId | String |

Response: This API call produces the following media types according to the CREATED request header and App Instance details in the response body.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description |
|------------------|-------------|
| 201              | Created     |

Response example:

````json
{
  "id": "7e151de6-18a9-4770-be4f-354b620f0035",
  "appId": "26471a81-1de4-4ad9-9724-326eefd22230",
  "status": "UNDEPLOYED",
  "createdAt": "2023-04-06T00:04:16.711+00:00",
  "updatedAt": "2023-04-06T00:05:16.711+00:00",
  "credentials": {
    "clientId": "rappid-3146ccdc-0323-4f34-8f3e-13b858c1c582-1708549936654-8c12ffcc-64c3-4070-b9b5-a115ded1f825"
  },
  "componentInstances": [
    {
      "name": "eric-oss-hello-metrics-go-app",
      "version": "1.1.1",
      "type": "Microservice",
      "deployState": "UNDEPLOYED",
      "properties": {
        "timeout": 5,
        "userDefinedHelmParameters": {
          "replicaCount": 2
        }
      }
    }
  ],
  "events": [],
  "self": {
    "href": "app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035"
  },
  "app": {
    "href": "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230"
  }
}
````

### Get All App Instances

Summary: This interface returns the information of all App Instances present in the system.

Path: GET /app-lifecycle-management/v3/app-instances

Header: Content-Type:application/json

Example:

GET /app-lifecycle-management/v3/app-instances

Body parameter: N/A

Response: This API call produces the following media types according to the OK request header and all the App Instances' details in the response body.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description |
|------------------|-------------|
| 200              | OK          |

Response example:

````json
[
  {
    "id": "7e151de6-18a9-4770-be4f-354b620f0035",
    "appId": "26471a81-1de4-4ad9-9724-326eefd22230",
    "status": "DEPLOYED",
    "createdAt": "2023-04-06T00:04:16.711+00:00",
    "updatedAt": "2023-04-06T00:05:16.711+00:00",
    "credentials": {
      "clientId": "rappid-3146ccdc-0323-4f34-8f3e-13b858c1c582-1708549936654-8c12ffcc-64c3-4070-b9b5-a115ded1f825"
    },
    "componentInstances": [
      {
        "name": "eric-oss-hello-metrics-go-app",
        "version": "1.1.1",
        "type": "Microservice",
        "deployState": "DEPLOYED",
        "properties": {
          "namespace": "namespace-example",
          "timeout": 5,
          "userDefinedHelmParameters": {
            "replicaCount": 2
          }
        }
      }
    ],
    "events": [],
    "self": {
      "href": "app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035"
    },
    "app": {
      "href": "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230"
    }
  }
]
````

### Get Specific App Instance

Summary: This interface returns information on a specific App Instance based on the id provided.

Path: GET /app-lifecycle-management/v3/app-instances/{appInstanceId}

Header: Content-Type:application/json

Example:

GET /app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035

Header parameter:

| Name          | Type   |
|---------------|--------|
| appInstanceId | String |

Body parameter: N/A

Response: This API call produces the following media types according to the OK request header and the App Instance's details in the response body.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description |
|------------------|-------------|
| 200              | OK          |

Response example:

````json
{
  "id": "7e151de6-18a9-4770-be4f-354b620f0035",
  "appId": "26471a81-1de4-4ad9-9724-326eefd22230",
  "status": "DEPLOYED",
  "createdAt": "2023-04-06T00:04:16.711+00:00",
  "updatedAt": "2023-04-06T00:05:16.711+00:00",
  "credentials": {
    "clientId": "rappid-3146ccdc-0323-4f34-8f3e-13b858c1c582-1708549936654-8c12ffcc-64c3-4070-b9b5-a115ded1f825"
  },
  "componentInstances": [
    {
      "name": "eric-oss-hello-metrics-go-app",
      "version": "1.1.1",
      "type": "Microservice",
      "deployState": "DEPLOYED",
      "properties": {
        "namespace": "namespace-example",
        "timeout": 5,
        "userDefinedHelmParameters": {
          "replicaCount": 2
        }
      }
    }
  ],
  "events": [],
  "self": {
    "href": "app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035"
  },
  "app": {
    "href": "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230"
  }
}
````

### Deploy/Undeploy/Upgrade App Instance

Summary: This interface allows for deployment related actions to be performed on a specific App instance which is identified by the provided Id, and in a specific namespace if it's specified. This includes the ability to DEPLOY, UNDEPLOY or UPGRADE the App instance.

Path: POST /app-lifecycle-management/v3/app-instances/{appInstanceId}/deployment-actions

Header: Content-Type:application/json

Deploy Example:

POST /app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035/deployment-actions

Header parameters:

| Name          | Type   |
|---------------|--------|
| appInstanceId | String |

Request body example:

````json
{
  "action": "DEPLOY",
  "componentInstances": [
    {
      "name": "eric-oss-hello-metrics-go-app",
      "version": "1.1.1",
      "type": "Microservice",
      "deployState": "UNDEPLOYED",
      "properties": {
        "timeout": 5,
        "userDefinedHelmParameters": {
          "replicaCount": 1
        },
        "namespace": "namespace-example"
      }
    }
  ]
}
````

Body parameters:

| Name               | Type       | Description                                                     | Requirement               | Option                                |
|--------------------|------------|-----------------------------------------------------------------|---------------------------|---------------------------------------|
| action             | String     | The type of call is defined here                                | Mandatory                 | - DEPLOY<br/>- UNDEPLOY<br/>- UPGRADE |
| componentInstances | collection | A collection of {componentInstances}                            | During Deploy and Upgrade |                                       |
| targetAppId        | String     | It is the new version of the App to upgrade the App instance to | Only during Upgrade       |                                       |

Component instance parameters:

| Name       | Type       | Description                                                            |
|------------|------------|------------------------------------------------------------------------|
| name       | String     | name of the component                                                  |
| properties | Collection | it is the properties used to configure the deployment of the component |

Properties parameters:

| Name                      | Type       | Description                                     |
|---------------------------|------------|-------------------------------------------------|
| timeout                   | Integer    | The time given before timing out the deployment |
| userDefinedHelmParameters | Collection | The Helm parameter settings                     |
| namespace                 | String     | Name of the namespace                           |


Response: This API call produces the following media types according to the CREATED request header and the App Instance's details in the response body.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description                                                 |
|------------------|-------------------------------------------------------------|
| 201              | Created - Deploy, Undeploy or Upgrade App Instance response |

Response example:

````json
{
  "id": "7e151de6-18a9-4770-be4f-354b620f0035",
  "appId": "26471a81-1de4-4ad9-9724-326eefd22230",
  "status": "DEPLOYING",
  "createdAt": "2023-04-06T00:04:16.711+00:00",
  "updatedAt": "2023-04-06T00:05:16.711+00:00",
  "credentials": {
    "clientId": "rappid-3146ccdc-0323-4f34-8f3e-13b858c1c582-1708549936654-8c12ffcc-64c3-4070-b9b5-a115ded1f825"
  },
  "componentInstances": [
    {
      "name": "eric-oss-hello-metrics-go-app",
      "version": "1.1.1",
      "type": "Microservice",
      "deployState": "DEPLOYED",
      "properties": {
        "timeout": 5,
        "userDefinedHelmParameters": {
          "replicaCount": 2
        },
        "namespace": "namespace-example"
      }
    }
  ],
  "events": [],
  "self": {
    "href": "app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035"
  },
  "app": {
    "href": "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230"
  }
}
````

### Update Specific App Instance

Summary: This interface allows for component instance properties of a given App instance to be updated. The full representation of the desired state of each component instance must be provided. Ideally a GET should be performed first, the complete representation of the existing components should be copied and then desired updates should be added to this representation.

Path: PUT /app-lifecycle-management/v3/app-instances/{appInstanceId}/component-instances

Header: Content-Type:application/json

Example:

PUT /app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035/component-instances

Header parameter:

| Name          | Type   |
|---------------|--------|
| appInstanceId | String |

Request body example:

````json
{
  "componentInstances": [
    {
      "name": "eric-oss-hello-metrics-go-app",
      "version": "1.1.1",
      "type": "Microservice",
      "deployState": "DEPLOYED",
      "properties": {
        "timeout": 15,
        "userDefinedHelmParameters": {
          "replicaCount": 3
        }
      }
    }
  ]
}
````

Component instances parameter:

| Name        | Type       | Required | Description                                     |
|-------------|------------|----------|-------------------------------------------------|
| name        | String     | yes      | The name of the component                       |
| version     | String     | no       | Version of the componentd                       |
| type        | String     | no       | Type of the component, can't be modified        |
| deployState | String     | no       | DeployState of the component, can't be modified |
| properties  | Collection | no       | The properties to configure the deployment      |

Properties parameters:

| Name                      | Type       | Description                                     |
|---------------------------|------------|-------------------------------------------------|
| timeout                   | Integer    | The time given before timing out the deployment |
| userDefinedHelmParameters | Collection | The helm parameter setting                      |

Response: This API call produces the following media types according to the ACCEPTED request header when the deployState is DEPLOYED else it would be OK request header and the App Instance's details in the response body.

The media type is conveyed by the Content-Type response header.

| HTTP Status Code | Description |
|------------------|-------------|
| 200              | OK          |
| 202              | Accepted    |

Response example:

````json
{
  "componentInstances": [
    {
      "name": "eric-oss-5gcnr",
      "version": "1.2.3",
      "type": "Microservice",
      "deployState": "UNDEPLOYED",
      "properties": {
        "timeout": 15,
        "userDefinedHelmParameters": {
          "replicaCount": 5
        }
      }
    }
  ],
  "appInstance": {
    "id": "7e151de6-18a9-4770-be4f-354b620f0035",
    "href": "app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035"
  }
}
````

### Delete App Instance
Summary: Permanently removes a specific App instance from the system.

Path: DELETE /app-lifecycle-management/v3/app-instances/{appInstanceId}

Header: Content-Type:application/json

Example:

DELETE /app-lifecycle-management/v3/app-instances/{appInstanceId}

Header parameter:

| Name          | Type   |
|---------------|--------|
| appInstanceId | String |

Body parameter: N/A

Response: This API call produces the following media types according to the ACCEPTED request header and some of the App Instance's details in the response body.

_Note: The ACCEPTED response only indicates that the request is accepted. The deletion of the App Instance will have to be monitored separately._

The media type is conveyed by the Content-Type response header.


| HTTP Status Code | Description |
|------------------|-------------|
| 204              | No Content  |

Response example:

````json
{
  "id": "7e151de6-18a9-4770-be4f-354b620f0035",
  "self": {
    "href": "app-lifecycle-management/v3/app-instances/7e151de6-18a9-4770-be4f-354b620f0035"
  }
}
````

## Contract Tests

Java classes are generated from an OpenAPI YAML description,
```eric-oss-app-lcm-openapi```. The OpenAPI is stored in the
project [Gerrit repository][gerrit].

The resulting .jar artifact is released and located in [Artifactory repository][artifactory].

The ```eric-oss-app-lcm-openapi``` for App LCM API includes resources and
contract tests.

Contract test stubs can be found [here][artifactory].

[gerrit]:<https://gerrit.ericsson.se/#/admin/projects/OSS/com.ericsson.oss.ae/eric-oss-app-lcm>
[artifactory]:<https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-release-local/com/ericsson/oss/ae/eric-oss-app-lcm/>
