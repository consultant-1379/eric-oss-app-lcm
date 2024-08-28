/**
 * Integration tests for <e-eric-oss-app-ui-app-artifacts>
 */
import EricOssAppUiAppArtifacts from "../../../src/apps/eric-oss-app-ui-app-artifacts/eric-oss-app-ui-app-artifacts.js";
import { expect, aTimeout, fixture, html } from "@open-wc/testing";
import { restCalls } from "../../../src/config/restCallsConfig";
import * as sinon from "sinon";

describe('App Artifacts Application Tests', () => {
  before(() => {
    EricOssAppUiAppArtifacts.register();
  }); 

  it('should have 4 columns', async () => {
    const tableElement = await fixture(html` <e-eric-oss-app-ui-app-artifacts></e-eric-oss-app-ui-app-artifacts>`);
    const euiTable = tableElement.shadowRoot.querySelector('eui-table');

    expect(euiTable.columns.length).to.eql(4);
  });
});
