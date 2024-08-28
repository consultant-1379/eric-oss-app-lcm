/**
 * Integration tests for <e-app-onboard>
 */
import { expect, aTimeout, fixture, html } from "@open-wc/testing";

import {
  injectHTMLElement
} from '../../utils/utils';
import AppOnboard from "../../../src/components/app-onboard/app-onboard";
import { restCalls } from "../../../src/config/restCallsConfig";
import * as sinon from "sinon";

describe('App Onboard Component Tests', () => {
  let container;
  let inject;
  before(() => {
    container = document.body.appendChild(document.createElement('div'));
    inject = injectHTMLElement.bind(null, container);
    AppOnboard.register();
  });

  after(() => {
    document.body.removeChild(container);
  });

  describe('Basic component setup', () => {
    it('should create a new <e-app-onboard>', async () => {
      const customElement = await inject('<e-app-onboard></e-app-onboard>');
      const actualElementRendered = container.querySelector('e-app-onboard');
      expect(actualElementRendered, '<e-app-onboard></e-app-onboard> was not found').to.equal(customElement);
    });

    it('should close and reset dialog', async () => {
      const onBoardDialog = await fixture(html` <e-app-onboard></e-app-onboard> `);

      onBoardDialog._closeOnboardDialog();

      expect(onBoardDialog.textBoxValue).to.eql('')
    });

    it('should open dialog', async () => {
      const onBoardDialog = await fixture(html` <e-app-onboard></e-app-onboard> `);

      onBoardDialog._openOnboardDialog();

      expect(onBoardDialog.dialogElement.show).to.eql(true)
    });

    it('should onboard "CSAR"', async () => {
      const onBoardDialog = await fixture(html` <e-app-onboard></e-app-onboard> `);

      onBoardDialog._onboardApp();

      expect(onBoardDialog.jobStatus).to.eql('UPLOADED')
    });

    it('should poll app-status', async () => {
      const onBoardDialog = await fixture(html` <e-app-onboard></e-app-onboard> `);

      onBoardDialog._appIsUploaded();

      expect(onBoardDialog.jobStatus).to.eql('CLEAN')
    });

    it('should add notification(onboarded)', async () => {
      const onBoardDialog = await fixture(html` <e-app-onboard></e-app-onboard> `);

      onBoardDialog._addNotification('ONBOARDED');

      expect(onBoardDialog.jobStatus).to.eql('UPLOADED')
    });

    it('should add notification(failed)', async () => {
      const onBoardDialog = await fixture(html` <e-app-onboard></e-app-onboard> `);

      onBoardDialog._addNotification('FAILED');

      expect(onBoardDialog.jobStatus).to.eql('UPLOADED')
    });
  });

  describe('Test App Status', () => {
    const mockTableBodyEnabled = {
      "items": [
          {
              "id": "79312ecd-aafc-40ba-8361-72bab62b47b0",
              "type": "rApp",
              "name": "Example App",
              "version": "1.0.2",
              "mode": "DISABLED",
              "status": "CREATED",
              "createdAt": "2024-03-13 14:32:04.375005",
              "components": [
                  {
                      "type": "MICROSERVICE",
                      "name": "eric-oss-app-onboarding",
                      "version": "0.1.0-1",
                      "artifacts": [
                          {
                              "name": "eric-oss-app-onboarding",
                              "type": "HELM",
                              "location": "9e6b3ffe-54de-4d48-92ed-a8122c1e5e19/eric-oss-app-onboarding-0.1.0-1.tgz"
                          },
                          {
                              "name": "busybox.tar",
                              "type": "IMAGE",
                              "location": "9e6b3ffe-54de-4d48-92ed-a8122c1e5e19/busybox.tar"
                          },
                          {
                              "name": "ASD.yaml",
                              "type": "OPAQUE",
                              "location": "9e6b3ffe-54de-4d48-92ed-a8122c1e5e19/ASD.yaml"
                          }
                      ]
                  }
              ],
              "permissions": [],
              "roles": [],
              "events": [],
              "self": {
                  "href": "/app-lifecycle-management/v3/apps/79312ecd-aafc-40ba-8361-72bab62b47b0"
              }
          },
          {
              "id": "9195cb39-8db1-4714-ac66-87b3394077df",
              "type": "rApp",
              "name": "Test App",
              "version": "1.0.0",
              "mode": "DISABLED",
              "status": "CREATED",
              "createdAt": "2024-03-13 14:28:38.576721",
              "components": [
                  {
                      "type": "MICROSERVICE",
                      "name": "eric-oss-app-onboarding",
                      "version": "0.1.0-1",
                      "artifacts": [
                          {
                              "name": "eric-oss-app-onboarding",
                              "type": "HELM",
                              "location": "55e1f58f-4e21-48ae-a8ec-ef59d71d58b9/eric-oss-app-onboarding-0.1.0-1.tgz"
                          },
                          {
                              "name": "busybox.tar",
                              "type": "IMAGE",
                              "location": "55e1f58f-4e21-48ae-a8ec-ef59d71d58b9/busybox.tar"
                          },
                          {
                              "name": "ASD.yaml",
                              "type": "OPAQUE",
                              "location": "55e1f58f-4e21-48ae-a8ec-ef59d71d58b9/ASD.yaml"
                          }
                      ]
                  }
              ],
              "permissions": [],
              "roles": [],
              "events": [],
              "self": {
                  "href": "/app-lifecycle-management/v3/apps/9195cb39-8db1-4714-ac66-87b3394077df"
              }
          },
          {
              "id": "7b21a709-f471-4c0a-b895-8d6d5fef299f",
              "type": "rApp",
              "name": "eric-oss-anr5gassist",
              "version": "1.0.51",
              "mode": "DISABLED",
              "status": "INITIALIZE_ERROR",
              "createdAt": "2024-03-12 12:09:44.191585",
              "components": [
                  {
                      "type": "MICROSERVICE",
                      "name": "eric-oss-anr5gassist",
                      "version": "1.0.51",
                      "artifacts": [
                          {
                              "name": "eris-oss-anr5gassist",
                              "type": "HELM",
                              "location": "dbea4e23-9786-4ee4-9c20-a66aaeddf0e6/eric-oss-anr5gassist-1.0.51.tgz"
                          },
                          {
                              "name": "docker.tar",
                              "type": "IMAGE",
                              "location": "dbea4e23-9786-4ee4-9c20-a66aaeddf0e6/docker.tar"
                          },
                          {
                              "name": "ASD.yaml",
                              "type": "OPAQUE",
                              "location": "dbea4e23-9786-4ee4-9c20-a66aaeddf0e6/ASD.yaml"
                          }
                      ]
                  }
              ],
              "permissions": [],
              "roles": [],
              "events": [
                  {
                      "type": "ERROR",
                      "title": "Initialize App Action request failure",
                      "detail": "Error while Priming AC Type definition in ACM-R. Error message from ACM-R: com.ericsson.oss.app.mgr.ac.element.AppLcmMicroserviceAutomationCompositionElement: Primed Failed\n",
                      "createdAt": "2024-03-12 12:15:58.496276"
                  }
              ],
              "self": {
                  "href": "/app-lifecycle-management/v3/apps/7b21a709-f471-4c0a-b895-8d6d5fef299f"
              }
          },
          {
              "id": "a0c22fbb-78cd-4705-a2ac-6c3da57a90e0",
              "type": "rApp",
              "name": "norway",
              "version": "1.0.13-10",
              "mode": "DISABLED",
              "status": "INITIALIZE_ERROR",
              "createdAt": "2024-03-11 10:41:52.412228",
              "components": [
                  {
                      "type": "MICROSERVICE",
                      "name": "eric-exil-pp-rad",
                      "version": "1.0.13-10",
                      "artifacts": [
                          {
                              "name": "docker.tar",
                              "type": "IMAGE",
                              "location": "c2570480-17bf-4d98-901a-85f811cf9c76/docker.tar"
                          },
                          {
                              "name": "ASD.yaml",
                              "type": "OPAQUE",
                              "location": "c2570480-17bf-4d98-901a-85f811cf9c76/ASD.yaml"
                          },
                          {
                              "name": "eric-exil-pp-rad",
                              "type": "HELM",
                              "location": "c2570480-17bf-4d98-901a-85f811cf9c76/eric-exil-pp-rad-1.0.13-10.tgz"
                          },
                          {
                              "name": "eric-exil-pp-rad-1.0.12-10.tgz",
                              "type": "OPAQUE",
                              "location": "c2570480-17bf-4d98-901a-85f811cf9c76/eric-exil-pp-rad-1.0.12-10.tgz"
                          }
                      ]
                  }
              ],
              "permissions": [],
              "roles": [],
              "events": [
                  {
                      "type": "ERROR",
                      "title": "Initialize App Action request failure",
                      "detail": "Error while Priming AC Type definition in ACM-R. Error message from ACM-R: com.ericsson.oss.app.mgr.ac.element.AppLcmMicroserviceAutomationCompositionElement: Primed Failed\n",
                      "createdAt": "2024-03-11 10:46:28.525159"
                  }
              ],
              "self": {
                  "href": "/app-lifecycle-management/v3/apps/a0c22fbb-78cd-4705-a2ac-6c3da57a90e0"
              }
          },
          {
              "id": "adc0b415-1d05-4d43-9d9f-26008864b889",
              "type": "rApp",
              "name": "App-Onboarding-helloWorld",
              "version": "1.0.4",
              "mode": "DISABLED",
              "status": "DELETE_ERROR",
              "createdAt": "2024-02-26 11:48:00.134817",
              "components": [
                  {
                      "type": "MICROSERVICE",
                      "name": "eric-oss-app-onboarding",
                      "version": "0.1.0-5",
                      "artifacts": [
                          {
                              "name": "eric-oss-app-onboarding",
                              "type": "HELM",
                              "location": "445ca97e-f7dc-40c0-82c7-ee1def980946/eric-oss-app-onboarding-0.1.0-1.tgz"
                          },
                          {
                              "name": "docker.tar",
                              "type": "IMAGE",
                              "location": "445ca97e-f7dc-40c0-82c7-ee1def980946/docker.tar"
                          },
                          {
                              "name": "ASD.yaml",
                              "type": "OPAQUE",
                              "location": "445ca97e-f7dc-40c0-82c7-ee1def980946/ASD.yaml"
                          }
                      ]
                  }
              ],
              "permissions": [
                  {
                      "resource": "kafka",
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
              "events": [
                  {
                      "type": "ERROR",
                      "title": "Delete App Action failure",
                      "detail": "Failed to remove artifacts",
                      "createdAt": "2024-02-27 11:43:40.080536"
                  }
              ],
              "self": {
                  "href": "/app-lifecycle-management/v3/apps/adc0b415-1d05-4d43-9d9f-26008864b889"
              }
          },
          {
              "id": "cf5980db-32e9-42cb-a1dd-a3b70ace1e99",
              "type": "rApp",
              "name": "App-Onboarding-helloWorld",
              "version": "1.0.0",
              "mode": "DISABLED",
              "status": "ONBOARDED",
              "createdAt": "2024-02-23 15:32:06.91137",
              "components": [
                  {
                      "type": "MICROSERVICE",
                      "name": "eric-oss-app-onboarding",
                      "version": "0.1.0-1",
                      "artifacts": [
                          {
                              "name": "eric-oss-app-onboarding",
                              "type": "HELM",
                              "location": "9ee7ef26-7ae3-4952-bbd7-c3da884a8b08/eric-oss-app-onboarding-0.1.0-1.tgz"
                          },
                          {
                              "name": "docker.tar",
                              "type": "IMAGE",
                              "location": "9ee7ef26-7ae3-4952-bbd7-c3da884a8b08/docker.tar"
                          },
                          {
                              "name": "ASD.yaml",
                              "type": "OPAQUE",
                              "location": "9ee7ef26-7ae3-4952-bbd7-c3da884a8b08/ASD.yaml"
                          }
                      ]
                  }
              ],
              "permissions": [
                  {
                      "resource": "kafka",
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
              "events": [
                  {
                      "type": "ERROR",
                      "title": "De-Initialize App Action request failure",
                      "detail": "Error while De-Priming Automation Composition Type definition in ACM-R. Error message from ACM-R: com.ericsson.oss.app.mgr.ac.element.AppLcmMicroserviceAutomationCompositionElement: Deprimed Failed. AcElementsDefinitions null or empty\n",
                      "createdAt": "2024-03-12 13:11:28.743745"
                  }
              ],
              "self": {
                  "href": "/app-lifecycle-management/v3/apps/cf5980db-32e9-42cb-a1dd-a3b70ace1e99"
              }
          }
      ]
  }

    let fetchStub;

    beforeEach (() => {
        fetchStub = sinon.stub(globalThis, 'fetch');
    })

    afterEach(() => {
        fetchStub.restore();
    });
    it('should check app status', async () => {
      const getTableDataRequest = `${restCalls.onBoarding.getOnboardingJobs.request}`;

      fetchStub.withArgs(getTableDataRequest).resolves(
          new Response(JSON.stringify(mockTableBodyEnabled), {
              status: 200,
              headers: {
                  'Content-type': 'application/json',
              },
          }),
      );
      const appUnderTest = new AppOnboard;

      //WHEN
      appUnderTest._checkJobStatus();

      await aTimeout(1000);

      //THEN
      sinon.assert.calledWith(fetchStub, getTableDataRequest);
      expect(appUnderTest.jobStatus, 'The app status is NOT returned').to.eql("ONBOARDED");
    });
  });

});