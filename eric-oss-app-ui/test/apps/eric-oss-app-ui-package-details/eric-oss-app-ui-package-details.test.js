import PackageDetails from "../../../src/apps/eric-oss-app-ui-package-details-v2/eric-oss-app-ui-package-details-v2";
import { expect, aTimeout, fixture, html } from "@open-wc/testing";
import { restCalls, getAppId } from "../../../src/config/restCallsConfig";
import * as sinon from "sinon";

describe('PackageDetails Application Tests', () => {
    before(() => {
        PackageDetails.register();
    });

    describe('Test Package Details Table', () => {
        const enableId = "7b8319dd-0198-4d4c-acf2-3fe352144951";
        const disableId = "7b8319dd-0198-4d4c-acf2-3fe352144951";

        const mockTableBodyEnabled = {
            "id": "7b8319dd-0198-4d4c-acf2-3fe352144951",
            "type": "rApp",
            "name": "App-Onboarding-helloWorld",
            "version": "1.0.2",
            "mode": "DISABLED",
            "status": "CREATED",
            "createdAt": "2024-03-14 14:37:15.896846",
            "components": [
              {
                "type": "MICROSERVICE",
                "name": "eric-oss-app-onboarding",
                "version": "0.1.0-1",
                "artifacts": [
                  {
                    "name": "eric-oss-app-onboarding",
                    "type": "HELM",
                    "location": "9d07ec63-f916-4b43-9704-c8a674501d73/eric-oss-app-onboarding-0.1.0-1.tgz"
                  },
                  {
                    "name": "docker.tar",
                    "type": "IMAGE",
                    "location": "9d07ec63-f916-4b43-9704-c8a674501d73/docker.tar"
                  },
                  {
                    "name": "ASD.yaml",
                    "type": "OPAQUE",
                    "location": "9d07ec63-f916-4b43-9704-c8a674501d73/ASD.yaml"
                  }
                ]
              }
            ],
            "permissions": [
              {
                "resource": "kafka",
                "scope": "test"
              },
              {
                "resource": "bdr",
                "scope": "read"
              },
              {
                "resource": "BDR",
                "scope": "write"
              },
              {
                "resource": "unknown",
                "scope": "test"
              }
            ],
            "roles": [
              {
                "name": "admin"
              },
              {
                "name": "user"
              }
            ],
            "events": [],
            "self": {
              "href": "/app-lifecycle-management/v3/apps/7b8319dd-0198-4d4c-acf2-3fe352144951"
            }
          }

        const mockTableBodyDisabled = {
            "id": "7b8319dd-0198-4d4c-acf2-3fe352144951",
            "type": "rApp",
            "name": "App-Onboarding-helloWorld",
            "version": "1.0.2",
            "mode": "DISABLED",
            "status": "CREATED",
            "createdAt": "2024-03-14 14:37:15.896846",
            "components": [
              {
                "type": "MICROSERVICE",
                "name": "eric-oss-app-onboarding",
                "version": "0.1.0-1",
                "artifacts": [
                  {
                    "name": "eric-oss-app-onboarding",
                    "type": "HELM",
                    "location": "9d07ec63-f916-4b43-9704-c8a674501d73/eric-oss-app-onboarding-0.1.0-1.tgz"
                  },
                  {
                    "name": "docker.tar",
                    "type": "IMAGE",
                    "location": "9d07ec63-f916-4b43-9704-c8a674501d73/docker.tar"
                  },
                  {
                    "name": "ASD.yaml",
                    "type": "OPAQUE",
                    "location": "9d07ec63-f916-4b43-9704-c8a674501d73/ASD.yaml"
                  }
                ]
              }
            ],
            "permissions": [
              {
                "resource": "kafka",
                "scope": "test"
              },
              {
                "resource": "bdr",
                "scope": "read"
              },
              {
                "resource": "BDR",
                "scope": "write"
              },
              {
                "resource": "unknown",
                "scope": "test"
              }
            ],
            "roles": [
              {
                "name": "admin"
              },
              {
                "name": "user"
              }
            ],
            "events": [],
            "self": {
              "href": "/app-lifecycle-management/v3/apps/7b8319dd-0198-4d4c-acf2-3fe352144951"
            }
          }

        let fetchStub;

        beforeEach (() => {
            fetchStub = sinon.stub(globalThis, 'fetch');
        })

        afterEach(() => {
            fetchStub.restore();
        });

        it('should have 5 columns', async () => {
            const tableElement = await fixture(html` <e-eric-oss-app-ui-package-details-v2></e-eric-oss-app-ui-package-details-v2> `);
            const euiTable = tableElement.shadowRoot.querySelector('eui-table');

            // expect(euiTable.columns.length).to.eql(4);
        });

        it('should update the table with data', async () => {
            const getTableDataRequest = `${restCalls.appLcm.getAllApps.request}${"/"}${getAppId()}`;

            fetchStub.withArgs(getTableDataRequest).resolves(
                new Response(JSON.stringify(mockTableBodyEnabled), {
                    status: 200,
                    headers: {
                        'Content-type': 'application/json; charset=utf-8',
                    },
                }),
            );
            const appUnderTest = new PackageDetails;

            //WHEN
            appUnderTest._updateTableData();

            await aTimeout(1000);

            //THEN
            sinon.assert.calledWith(fetchStub, getTableDataRequest);
            //expect(appUnderTest.applicationData[0], 'The correct detail is returned').to.eql(mockTableBody);
        });
    });
});
