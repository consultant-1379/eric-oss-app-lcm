/**
 * Integration tests for <e-app-table>
 */
import AppAdmin from "../../../src/apps/eric-oss-app-ui/eric-oss-app-ui";

import { expect, fixture, html } from "@open-wc/testing";

import {
  injectHTMLElement
} from '../../utils/utils';

describe('App Admin Tests', () => {
  let container;
  let inject;
  before(() => {
    container = document.body.appendChild(document.createElement('div'));
    inject = injectHTMLElement.bind(null, container);
  });

  after(() => {
    document.body.removeChild(container);
  });

  describe('Basic component setup', () => {
    it('should create a new <e-eric-oss-app-ui>', async () => {
      const customElement = await inject('<e-eric-oss-app-ui></e-eric-oss-app-ui>');
      const actualElementRendered = container.querySelector('e-eric-oss-app-ui');
      expect(actualElementRendered, '<e-eric-oss-app-ui></e-eric-oss-app-ui> was not found').to.equal(customElement);
    });
  });
});