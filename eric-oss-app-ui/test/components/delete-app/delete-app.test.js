/**
 * Integration tests for <e-delete-app>
 */
import "../../../src/components/delete-app/delete-app-v1"
import { expect } from "@open-wc/testing";

import {
  injectHTMLElement
} from '../../utils/utils';

describe('Delete App Component Tests', () => {
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
    it('should create a new <e-delete-app-v1>', async () => {
      const customElement = await inject('<e-delete-app-v1></e-delete-app-v1>');
      const actualElementRendered = container.querySelector('e-delete-app-v1');
      expect(actualElementRendered, '<e-delete-app-v1></e-delete-app-v1> was not found').to.equal(customElement);
    });

    it('should delete the app', async () => {
      const deleteAppElement = await inject('<e-delete-app-v1></e-delete-app-v1>');

      deleteAppElement._executeDeleteApp();

      expect(deleteAppElement.appInstaniteStatus).to.eql('TERMINATED');
    });

    it('should create a notification (Enabled)', async () => {
      const deleteAppElement = await inject('<e-delete-app-v1></e-delete-app-v1>');

      deleteAppElement._addNotification('Enabled');
      deleteAppElement._addNotification('Deleted');
      deleteAppElement._addNotification('Deleting');
      deleteAppElement._addNotification('AllOther');

      expect(deleteAppElement.applicationName).to.eql('');
    });
  });

});