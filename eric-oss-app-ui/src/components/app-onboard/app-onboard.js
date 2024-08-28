/**
 * Component AppOnboard is defined as
 * `<e-app-onboard>`
 *
 * @extends {LitComponent}
 */
import { LitComponent, html, definition } from '@eui/lit-component';
import { Button, FileInput, TextField } from '@eui/base';
import { Loader } from '@eui/base/loader';
import { Notification } from '@eui/base/notification';
import { Dialog } from '@eui/base/dialog';
import { restCalls } from '../../config/restCallsConfig.js';
import style from './app-onboard.css';
import { hasResource } from '../../utils/api/rbac';
import { POST } from "../../utils/constants"
import { REQUIRED_RESOURCE_V2_ONBOARDING } from "../../utils/resources";

export default class AppOnboard extends LitComponent {
  static get components() {
    return {
      'eui-button': Button,
      'eui-dialog': Dialog,
      'eui-file-input': FileInput,
      'eui-notification': Notification,
      'eui-text-field': TextField,
      'eui-loader': Loader,
    };
  }

  didUpgrade() {
    this.dialogElement = this.shadowRoot.querySelector('eui-dialog');
    this.spinnerElement = this.shadowRoot.querySelector('#onboard_spinner');
    this.tableElement = this.shadowRoot.querySelector('eui-table');
    this.fileInputElement = this.shadowRoot.querySelector('eui-file-input');
  }

  didConnect() {
    this._checkOnboardPermissions()
      .then(canOnboard => {
        this.canUserOnboard = canOnboard;

        // Set Up Onboard Button
        const onboardButtonElement = this.createElement('eui-button');
        onboardButtonElement.textContent = 'Onboard an App';
        onboardButtonElement.id = 'onboard-btn';
        onboardButtonElement.addEventListener(
          'click',
          this._openOnboardDialog,
          true,
        );

        if(this.canUserOnboard){
          this.bubble('app:actions', {
            actions: [onboardButtonElement],
          });
        }
      })
      .catch(e => {
        console.error(e);
      });

    window.addEventListener('beforeunload', e => {
      if (e.origin !== undefined) { return; }

      if (this.uploadInProgress) {
        e.preventDefault();
        (e || window.event).returnValue = null;
      }
    });

    window.addEventListener('eui-dialog:cancel', e => {
      if (e.origin !== undefined) { return; }
      this.shadowRoot.querySelector('#onboard_spinner').style.display = "none";
    });

    this.jobStatus = "UPLOADED";
    this.textBoxValue = "";
  }

  _checkOnboardPermissions = async () => {
    return hasResource(REQUIRED_RESOURCE_V2_ONBOARDING, [POST]);
  }

  _openOnboardDialog = () => {
    this.dialogElement.show = true;
    this.fileInput = "";
    this.textBoxValue = "";
    this.shadowRoot.querySelector('#startOnboard').setAttribute('disabled', true);
  };

  _onboardApp = () => {
    this.jobStatus = "UPLOADED";
    this.shadowRoot.querySelector('#startOnboard').disabled = true;
    this.fileInput = this.fileInputElement.value;
    let fileExtension = this.fileInput.slice(this.fileInput.lastIndexOf(".") + 1)
    let file = this.fileInput.split('\\').pop().split('/').pop();
    let fileName = file.slice(0, file.lastIndexOf("."));
    if (fileExtension !== "csar") {
      this.shadowRoot.querySelector('#startOnboard').setAttribute('disabled', true);
      this._addNotification('FILETYPE_ERROR');
      this.textBoxValue = "";
    }
    else if (fileName.includes("..") || fileName.includes("/") || fileName.includes("\\")) {
      this.shadowRoot.querySelector('#startOnboard').setAttribute('disabled', true);
      this._addNotification('FILENAME_ERROR');
      this.textBoxValue = "";
    }
    else {
      this.textBoxValue = this.fileInput.substring(this.fileInput.lastIndexOf("\\") + 1);
      this.shadowRoot.querySelector('#startOnboard').removeAttribute('disabled');
    }
    this.fileInput = "";
  };

  _addNotification = (status) => {
    const notification = this.createElement('eui-notification');
    notification.timeout = 10000;

    const notificationIcon = document.createElement('eui-icon');
    notificationIcon.size = '18px'
    notificationIcon.style = 'display: inline-block; vertical-align: middle;'
    notification.textContent = 'App Onboarding';

    if (status === 'FAILED') {
      notificationIcon.name = 'cross';
      notificationIcon.setAttribute('color', 'red');
      notification.description = "Application has failed";

    } else if (status === 'ONBOARDED') {
      notificationIcon.name = 'check';
      notificationIcon.setAttribute('color', 'green');
      notification.description = "Application has successfully onboarded";

    } else if (status === 'FILETYPE_ERROR') {
      notificationIcon.name = 'cross';
      notificationIcon.setAttribute('color', 'red');
      notification.description = "Unsupported file type. Please select a \".csar\" file";

    } else if (status === 'FILENAME_ERROR') {
      notificationIcon.name = 'cross';
      notificationIcon.setAttribute('color', 'red');
      notification.description = "Invalid file name. Please rename the \".csar\" file";

    }
    else {
      notificationIcon.name = 'check';
      notificationIcon.setAttribute('color', 'green');
      notification.description = "Application has successfully uploaded. Onboarding process has now started";
    }
    notification.prepend(notificationIcon);
    notification.showNotification();
  };

  /**
   * Searches for most recent App Onboarding entry to check for App status change
   *
   * @function _checkAppStatus
   * @private
  */
  _checkJobStatus = async () => {
    const getJobs = restCalls.onBoarding.getOnboardingJobs; // eslint-disable-line
    fetch(`${getJobs.request}`, { method: 'GET', headers: { "Content-Type": "application/json; charset=utf-8" } })
      .then(res => res.json())
      .then(response => {
        this.jobStatus = response.items[response.items.length - 1].status; //eslint-disable-line
        this.jobStatus === "UPLOADED" || "FAILED" || "PARSED" ? this._appIsUploaded(this.jobStatus) : console.info("INFO: No change: " + this.jobStatus);
      })
      .catch(err => {
        console.error(`${'Error: '}${err}`) // eslint-disable-line
      });
  }

  _appIsUploaded = (onboardingStatus) => {
    console.info("Finished!:" + this.jobStatus);
    this._closeOnboardDialog(onboardingStatus);
  }

  /**
   * Opens dialog box to Onboard an App Package
   *
   * @function _clickOnBoard
   * @private
  */
  _clickOnBoard = (e) => {
    this.spinnerElement.style.display = "flex";
    this.shadowRoot.querySelector('#startOnboard').disabled = true;
    this.dialogElement.shadowRoot.querySelector('.cancel').disabled = true;
    this.fileInputElement.disabled = true;

    const input = this.fileInputElement.files[0];
    const data = new FormData();
    const postApp = restCalls.onBoarding.onboardApp; // eslint-disable-line
    data.append('file', input, input.File);
    fetch(`${postApp.request}`, { method: 'POST', body: data })
      .then(response => {
        if (response.status === 202) {
          this._appIsUploaded('ONBOARDING');
        }
        else {
          this._appIsUploaded('FAILED');
        }
      })
      .catch(err => {
        console.log(`${'Error: '}${err}`) // eslint-disable-line
        this._appIsUploaded('FAILED');
      });
  };

  /**
   * Closes dialog once upload condition has been met. Resets dialog elements to default.
   *
   * @function _closeOnboardDialog
   * @private
  */
  _closeOnboardDialog = (onboardingStatus) => {
    this._addNotification(onboardingStatus);
    this.uploadInProgress = true;
    this.textBoxValue = "";
    this.fileInputElement.removeAttribute("value");
    this.dialogElement.removeAttribute('show');
    this.spinnerElement.style.display = "none";
    this.shadowRoot.querySelector('#startOnboard').disabled = false;
    this.dialogElement.shadowRoot.querySelector('.cancel').disabled = false;
    this.fileInputElement.disabled = false;
    this.jobStatus = "CLEAN"
  }

  render() {
    return html`
      <eui-dialog label="Onboard App Package" id="onboardDialog">
        <div slot="content">
          <p>Select the app package to onboard</p>
          <div id="onboard_inputs">
            <eui-text-field data-cy="onboard__textField" name="item" id="upload-csar" .value="${this.textBoxValue}" size="32" disabled></eui-text-field>
            <eui-file-input data-cy="onboard__fileInput" name="file" id="onboardFile" @change="${this._onboardApp}" accept=".csar">
              Choose file
            </eui-file-input>
          </div>
          <div id="onboard_spinner">
            <eui-loader size="medium"></eui-loader>
            &nbsp;&nbsp;
            Uploading App Package...
          </div>
        </div>
        <eui-button data-cy="onboard__button" id="startOnboard" @click="${e => this._clickOnBoard(e)}" slot="bottom" primary disabled>Onboard</eui-button>
      </eui-dialog>
     `;
  }
}

/**
 * @property {String} textBoxValue - default value for the onboarding modal's text box
 */
definition('e-app-onboard', {
  style,
  props: {
    canUserOnboard: { attribute: false, type: Boolean, default: true },
    fileInput: { type: String, default: '' },
    textBoxValue: { type: String, default: '' },
    uploadInProgress: { default: false },
    jobStatus: { type: String, default: 'CLEAN' },
  },
})(AppOnboard);