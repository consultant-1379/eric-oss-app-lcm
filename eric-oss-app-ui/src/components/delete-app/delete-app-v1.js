/**
 * Component DeleteAppV1 is defined as
 * `<e-delete-app-v1>`
 *
 * @extends {LitComponent}
 */
import { LitComponent, html, definition } from '@eui/lit-component';
import { Button } from "@eui/base";
import { Notification } from '@eui/base/notification';
import { Dialog } from "@eui/base/dialog";
import { restCallsV1, getAppIdV1 } from '../../config/restCallsConfigV1.js';
import style from './delete-app-v1.css';
import { hasResource } from '../../utils/api/rbac';
import { DELETE } from "../../utils/constants"
import { REQUIRED_RESOURCE_V1_LCM_DELETE } from "../../utils/resources";

export default class DeleteAppV1 extends LitComponent {
  // Uncomment this block to add initialization code
  // constructor() {
  //   super();
  //   // initialize
  // }
  static get components() {
    return {
      // register components here
      'eui-button': Button,
      'eui-dialog': Dialog,
      'eui-notification': Notification,
    };
  }
  didUpgrade() {
    this.deleteDialog = this.shadowRoot.querySelector('#deleteDialog');
    this.instatiatedDialog = this.shadowRoot.querySelector('#instantiatedDialog')
    this.tableElement = this.shadowRoot.querySelector('eui-table');
  }
  didConnect() {
    this._checkDeletePermissions()
      .then(canDelete => {
        this.canUserDelete = canDelete;

        // create the delete button...
        const deleteButtonElement = this.createElement('eui-button');
        deleteButtonElement.textContent = 'Delete App';
        deleteButtonElement.primary = false;
        deleteButtonElement.id = 'delete-app-btn';
        deleteButtonElement.addEventListener('click', this._executeDeleteApp, true);
        
        if(this.canUserDelete){
          this.bubble('app:actions', {
            actions: [deleteButtonElement],
          });
        }
      })
      .catch(e => {
        console.error(e);
      });
    this._checkHealthStatus();
  }

  _checkDeletePermissions = async () => {
    return hasResource(REQUIRED_RESOURCE_V1_LCM_DELETE, [DELETE]);
  }

  _checkHealthStatus = () => {
    const getURL = `${restCallsV1.appLcm.getAppInstance.request}${"/"}${getAppIdV1()}`;
    fetch(`${getURL}`, {
      method: 'GET',
      headers: { "Content-Type": "application/json; charset=utf-8" },
    }).then((results => results.json())).then(status => {
      this.appInstaniteStatus = status.healthStatus;
    })
  }
  _executeDeleteApp = () => { // eslint-disable-line
    this.appInstaniteStatus === 'INSTANTIATED' // eslint-disable-line
      ? this.instatiatedDialog.setAttribute('show', true)
      : this.deleteDialog.setAttribute('show', true);
  };
  _addNotification = (message) => {
    const notification = this.createElement('eui-notification');
    notification.timeout = 5000;
    const icon = document.createElement('eui-icon');
    icon.size = '18px'
    icon.style = 'display: inline-block; vertical-align: middle;'
    if (message === 'Enabled') {
      notification.description = `Application is currently Enabled. Please disable application to perform delete.`;
      notification.textContent = ` ${this.applicationName} failed to Delete`;
      icon.name = 'cross'
      icon.setAttribute('color', 'red');
    } else if (message === 'Deleted') {
      notification.description = 'Application and it\'s dependencies have been deleted';
      notification.textContent = ` ${this.applicationName} successfully Deleted.`;
      icon.name = 'check'
      icon.setAttribute('color', 'green');
    } else if (message === 'Deleting') {
      notification.description = 'Application cannot be fully deleted, as there are active instances. Please disable all instances to delete';
      notification.textContent = ` ${this.applicationName} is deleting`;
      icon.name = 'triangle-warning'
      icon.setAttribute('color', 'orange');
    } else {
      notification.description = 'Failure to Delete. Reason unknown! Please contact Administrator.';
      notification.textContent = ` ${this.applicationName} failed to Delete`;
      icon.name = 'cross'
      icon.setAttribute('color', 'red');
    }
    notification.prepend(icon);
    // eslint-disable-line
    notification.addEventListener('click', event => { // eslint-disable-line
      console.log('clicked'); // eslint-disable-line
    });
    notification.showNotification();
  }
  _deleteApp = () => {
    const getURL = `${restCallsV1.appLcm.deleteApp.request}${"/"}${getAppIdV1()}`;
    fetch(`${getURL}`, {
      method: 'DELETE',
      headers: { "Content-Type": "application/json; charset=utf-8" },
    }).then((results) => {
      this.deleteDialog.removeAttribute('show');
      if (results.status === 202) {
        window.EUI.Router.goto('/#eric-oss-app-ui');
        this._addNotification('Deleted');
      } else if (results.status === 409) {
        this._addNotification('Enabled');
      } else if (results.status === 200) {
        window.EUI.Router.goto('/#eric-oss-app-ui');
        this._addNotification('Deleting');
      } else {
        this._addNotification('Other');
      }
    })
  };
  /**
   * Render the <e-delete-app-v1> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <eui-dialog label="Delete Application" data-cy="dialog-box" id="deleteDialog">
        <div slot="content">
          <p>This action will permanently delete the App Package and all contents.</p>
          <p>You will unable to interact with the application. Application will need to be onboarded again.</p>
          <p>Do you want to continue?</p>
        </div>
        <eui-button slot="bottom" warning data-cy="delete-btn"
          @click="${event => this._deleteApp() /* eslint-disable-line */}">Delete</eui-button>
      </eui-dialog>
      <eui-dialog label="Instantiated Application" data-cy="dialog-box" id="instantiatedDialog">
        <div slot="content">
          <p>Application cannot be deleted, as there are active instances.</p>
          <p>Disable all instances to delete</p>
        </div>
      </eui-dialog>
    `
  }
}
/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {String} propTwo - shows the "Hello World" string.
 */
definition('e-delete-app-v1', {
  style,
  props: {
    canUserDelete: { attribute: false, type: Boolean, default: true },
    applicationData: { attribute: false, type: Array, default: [] },
    applicationName: { attribute: true, type: String, default: '' },
    appInstaniteStatus: { attribute: true, type: String, default: 'TERMINATED' },
  },
})(DeleteAppV1);
DeleteAppV1.register();