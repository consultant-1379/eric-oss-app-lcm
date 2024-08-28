/**
 * Integration tests for <e-job-table>
 */
import JobTable from '../../../src/components/job-table/job-table.js';
import { expect, fixture, html, aTimeout } from "@open-wc/testing";
import { injectHTMLElement} from '../../utils/utils.js';
import * as sinon from "sinon";
import { restCalls } from "../../../src/config/restCallsConfig.js";

describe('Job Table Component Tests', () => {
  let container;
  let inject;
  before(() => {
    container = document.body.appendChild(document.createElement('div'));
    inject = injectHTMLElement.bind(null, container);
    JobTable.register();
  });

  after(() => {
    document.body.removeChild(container);
  });

  describe('Basic component setup', () => {
    it('should create a new <e-job-table>', async () => {
      const customElement = await inject('<e-job-table></e-job-table>');
      const actualElementRendered = container.querySelector('e-job-table');
      expect(actualElementRendered, '<e-job-table></e-job-table> was not found').to.equal(customElement);
    });

    it('should have 9 columns', async () => {
      const tableElement = await fixture(html` <e-job-table></e-job-table> `);
      const euiTable = tableElement.shadowRoot.querySelector('eui-table');

      expect(euiTable.columns.length).to.eql(9);
    });
  });

  describe('Job Table Rendering', () => {
    const getAllJobsMocked = {
      "items": [
        {
          "id": "9cc1047a-5aae-4630-893a-1536392cbd2b",
          "fileName": "eric-oss-hello-world-app.csar",
          "packageVersion": "1.1.1",
          "packageSize": "100MiB",
          "vendor": "Ericsson",
          "type": "rApp",
          "onboardStartedAt": "2023-12-20T12:00:06.996762Z",
          "status": "ONBOARDED",
          "onboardEndedAt": "2023-12-20T12:00:54.798965Z",
          "events": [
            {
              "type": "INFO",
              "title": "Stored 1 out of 3 artifacts",
              "detail": "Uploaded eric-oss-hello-world-app",
              "occurredAt": "2023-12-20T12:00:47.937804Z"
            },
            {
              "type": "INFO",
              "title": "Stored 2 out of 3 artifacts",
              "detail": "Uploaded docker.tar",
              "occurredAt": "2023-12-20T12:00:48.063780Z"
            },
            {
              "type": "INFO",
              "title": "Stored 3 out of 3 artifacts",
              "detail": "Uploaded ASD.yaml",
              "occurredAt": "2023-12-20T12:00:48.110092Z"
            }
          ],
          "self": {
            "href": "app-onboarding/v2/onboarding-jobs/9cc1047a-5aae-4630-893a-1536392cbd2b"
          },
          "app": {
            "id": "26471a81-1de4-4ad9-9724-326eefd22230",
            "href": "app-lifecycle-management/v3/apps/26471a81-1de4-4ad9-9724-326eefd22230"
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

    it('should update the jobs table with data', async () => {
      const getTableDataRequest = `${restCalls.onBoarding.getOnboardingJobs.request}`;

      fetchStub.withArgs(getTableDataRequest).resolves(
          new Response(JSON.stringify(getAllJobsMocked), {
              status: 200,
              headers: {
                  'Content-type': 'application/json; charset=utf-8',
              },
          }),
      );
      const jobUnderTest = new JobTable;

      //WHEN
      jobUnderTest._updateJobsTable();

      await aTimeout(1000);

      let data = [];
      getAllJobsMocked.items.forEach((job) => {
        const newRow = {
          col1: job.fileName,
          col2: job.packageVersion,
          col3: job.packageSize,
          col4: job.vendor,
          col5: job.app.id,
          col6: job.id,
          col7: { status: job.status },
          col8: new Date(job.onboardStartedAt).toUTCString(),
          col9: new Date(job.onboardEndedAt).toUTCString(),
        }
        data = [...data, newRow];
      })

      //THEN
      sinon.assert.calledWith(fetchStub, getTableDataRequest);

      expect(jobUnderTest.jobs, 'The correct detail is returned').to.eql(data);
    });
  });
})