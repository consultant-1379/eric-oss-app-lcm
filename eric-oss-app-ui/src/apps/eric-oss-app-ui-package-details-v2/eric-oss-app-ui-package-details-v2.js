/**
 * EricOssAppUiPackageDetailsV2 is defined as
 * `<e-eric-oss-app-ui-package-details-v2>`
 *
 * Imperatively create application
 * @example
 * let app = new EricOssAppUiPackageDetailsV2();
 *
 * Declaratively create application
 * @example
 * <e-eric-oss-app-ui-package-details-v2></e-eric-oss-app-ui-package-details-v2>
 *
 * @extends {App, AppTable}
 */
import { App, html, definition } from '@eui/app';
import { Theme } from '@eui/theme';
import { Switch, Dropdown, InfoPopup } from '@eui/base';
import { Table } from "@eui/table";
import { Icon } from '@eui/theme/icon';
import { Tile } from '@eui/layout/tile';
import { Card } from '@eui/layout/card';
import { Link } from '@eui/base/link';
import { Loader } from '@eui/base/loader';
import { Dialog } from "@eui/base/dialog";
import { Tooltip } from '@eui/base/tooltip';
import { Notification } from '@eui/base/notification';
import { Button } from "@eui/base";
import style from './eric-oss-app-ui-package-details-v2.css';
import { restCalls, getAppId } from '../../config/restCallsConfig.js';
import { hasResource } from '../../utils/api/rbac';
import { GET, DELETE } from "../../utils/constants"
import { REQUIRED_RESOURCE_V3_LCM } from "../../utils/resources";


export default class EricOssAppUiPackageDetailsV2 extends App {
    constructor() {
      super();
  }

  static get components() {
    return {
      // register components here
      'eui-button': Button,
      'eui-icon': Icon,
      'eui-switch': Switch,
      'eui-dropdown': Dropdown,
      'eui-table': Table,
      'eui-tile': Tile,
      'eui-card': Card,
      'eui-link': Link,
      'eui-dialog': Dialog,
      'eui-notification': Notification,
      'eui-info-popup': InfoPopup,
      'eui-theme': Theme,
      'eui-loader': Loader,
      'eui-tooltip': Tooltip,
    };
  }

  didUpgrade() {
    this.tableElement = this.shadowRoot.querySelector('eui-table');
    this.switchElement = this.shadowRoot.querySelector('eui-switch');
    this.deleteDialog = this.shadowRoot.querySelector('#deleteDialog');
    this.initializedDialog = this.shadowRoot.querySelector('#initializedDialog');
    this.deleteErrorDialog = this.shadowRoot.querySelector('#deleteErrorDialog');
    this.instantiatedDialog = this.shadowRoot.querySelector('#instantiatedDialog');
    this.deinitializeDialog = this.shadowRoot.querySelector('#deinitializeDialog');
    this.initializeButtonElement = this.shadowRoot.querySelector('#initialize-app-btn');
    this.initializeButtonElementTooltip = this.shadowRoot.querySelector('#initialize-app-btn-tooltip');
    this.deleteButtonElement = this.shadowRoot.querySelector('#delete-app-btn');
    this.infoPopup = this.shadowRoot.querySelector('eui-info-popup');
  }

  didConnect() {
    document.title = 'App Manager - Package Details';
    this._pollTableData();

    this._checkAccessPrivileges()
      .then(canViewUI => {
        if(!canViewUI){
          this.canUserViewUI = false;
        }
      })
      .catch(e => {
        console.error(e);
      });

    hasResource(REQUIRED_RESOURCE_V3_LCM, [DELETE])
      .then(isAdmin => {
        this.canUserDelete = isAdmin;
      });
  }
/* c8 ignore next 35 */
  _initializeDeInitializeApp = (shouldOpenDeinitDialog) => {
    if (this.initializeButtonElement.textContent === 'Initialize App') {
      this.updateButtonAttributes("Initializing App", "set", "set", "reload", "inline");
      this.appStatus = "INITIALIZING"
      this._executeInitializeApp('INITIALIZE');
    } else {
      if (shouldOpenDeinitDialog) {
        this.deinitializeDialog.setAttribute("show", true)
        return;
      }
      this.updateButtonAttributes("De-initializing App", "set", "set", "reload", "inline");
      this.appStatus = "DEINITIALIZING"
      this.deinitializeDialog.removeAttribute("show");
      this._executeInitializeApp('DEINITIALIZE');
    }
  }

  _executeInitializeApp = async (status) => { // eslint-disable-line
    let body = {};
    const postURL = `${restCalls.appLcm.initApp.request}${"/"}${getAppId()}${"/initialization-actions"}`;
    if (status === 'INITIALIZE') {
      body.action = "INITIALIZE";
    } else {
      body.action = "DEINITIALIZE";
    }
    let response;
    try {
      response = await fetch(`${postURL}`, {
      method: 'POST',
      headers: { "Content-Type": "application/json; charset=utf-8" },
      body: JSON.stringify(body)
      }).then((results => results.json()));
    } catch(error){
      console.error(error);
    }

    if(response.status === 500) {
      this.instantiatedDialog.setAttribute('show', true);
      this.updateButtonAttributes("De-initialize App", "set", "remove", "icon", "inline");
    }
  };

  updateButtonAttributes = (text, initSetRemove, deleteSetRemove, icon, popupDisplay) => {
    this.initializeButtonElement.textContent = text;
    if (initSetRemove === "set") {
      this.initializeButtonElement.setAttribute('disabled');
    } else {
      this.initializeButtonElement.removeAttribute('disabled');
    }

    if (deleteSetRemove === "set") {
      this.deleteButtonElement.setAttribute('disabled');
    } else {
      this.deleteButtonElement.removeAttribute('disabled');
    }

    if (icon === "icon") {
      this.initializeButtonElement.removeAttribute(icon);
    } else {
      this.initializeButtonElement.icon = "reload";
    }
    this.infoPopup.style.display = popupDisplay;
  }

/* c8 ignore next 38 */
  updateButtons = () => {
    if (this.appMode === 'DISABLED' && this.appStatus === 'CREATED' || this.appStatus === 'DEINITIALIZE_ERROR') {
      this.updateButtonAttributes("Initialize App", "remove", "remove", "icon", "inline");
    } else if (this.appMode === 'DISABLED' && this.appStatus === 'INITIALIZED') {
      this.updateButtonAttributes("De-initialize App", "remove", "remove", "icon", "none");
    } else if (this.appMode === 'DISABLED' && this.appStatus === 'INITIALIZING') {
      this.updateButtonAttributes("Initializing App", "set", "set", "reload", "inline");
    } else if (this.appMode === 'DISABLED' && this.appStatus === 'DEINITIALIZING') {
      this.updateButtonAttributes("De-initializing App", "set", "set", "reload", "inline");
    } else if (this.appMode === 'ENABLED' && this.appStatus === 'INITIALIZED'){
      this.updateButtonAttributes("De-initialize App", "remove", "remove", "icon", "none");
    } else {
      this.updateButtonAttributes("Initialize App", "set", "remove", "icon", "inline");
    }

    this.bubble('app:actions', {
      actions: [this.initializeButtonElementTooltip, this.deleteButtonElement],
    });
  }

    /**
   * Updates the List of Apps asynchronously at 10 second intervals
   *
   * @function _pollTableData
   * @private
  */
    _pollTableData = async () => {
      const POLL_INTERVAL = 10000;
      try {
        await this._updateTableData();
        const getTime = new Date(Date.now()).toUTCString();
        this.lastUpdate = `Last Update: ${getTime}`;

        this.pollingTimer = setTimeout(this._pollTableData, POLL_INTERVAL);
      } catch (err) {
        // eslint-disable-next-line no-console
        console.log(`Get list of Apps failed`, err);
      }
    }

  _updateTableData = () => {
    const getURL = `${restCalls.appLcm.getAllApps.request}${"/"}${getAppId()}`;
      fetch(`${getURL}`, { headers: { "Content-Type": "application/json; charset=utf-8" } })
        .then(res => res.json())
        .then(response => {
          this.appData = [...this.appData, response];
          this.appStatus = response.status;
          this.appMode = response.mode;
          this.updateButtons();
          this.appName = response.name;
          this.appVersion = response.version;

          this.appComponents = [];
          for (let component in response.components) {
            const newRow = {
              col1: { id: response.id, name: response.components[component].name },
              col2: response.components[component].type,
              col3: response.components[component].version,
              col4: response.components[component].artifacts.length
            }
            this.appComponents = [...this.appComponents, newRow];
          }

          if (this.appStatus !== 'INITIALIZED') {
            this.switchElement.setAttribute("disabled");
          } else {
/* c8 ignore next 2 */
            this.switchElement.removeAttribute("disabled");
          }
/* c8 ignore next 20 */
          if (this.appMode === 'ENABLED') {
            this.switchElement.on = true;
            this.initializeButtonElement.setAttribute("disabled");
          } else {
            this.switchElement.on = false;
          }

          if (this.appStatus === "DEINITIALIZED") {
            this.isDeinitTooltipVisible = "";
          } else {
            this.isDeinitTooltipVisible = "never";
          }

          this.bubble('app:breadcrumb', {
              breadcrumb:
                [
                  {
                    displayName: 'EIC',
                  },
                  {
                    displayName: 'App Administration',
                    action: () => { window.EUI.Router.goto('/#eric-oss-app-ui') },
                  },
                  {
                    displayName: this.appName,
                  },
                ],
            },
          )
        })
        .catch(err => {
          console.log(`${'Error: '}${err}`); // eslint-disable-line
        });
  }

  _updateMode = (mode) => { // eslint-disable-line
    !mode // eslint-disable-line
      ? this._enableDisable({ mode: 'DISABLED' })
      : this._enableDisable({ mode: 'ENABLED' })
  }
/* c8 ignore next 16 */
  _enableDisable = (enableDisable) => {
    if(enableDisable.mode === 'DISABLED') {
      this.initializeButtonElement.removeAttribute('disabled');
      this.appMode = 'DISABLED';
    } else {
      this.initializeButtonElement.setAttribute('disabled');
      this.appMode = 'ENABLED';
    }
    const putURL = `${restCalls.appLcm.getAllApps.request}${"/"}${getAppId()}${"/mode"}`;
    fetch(`${putURL}`, {
      method: 'PUT',
      headers: { "Content-Type": "application/json; charset=utf-8" },
      body: JSON.stringify(enableDisable),
    }).then(res => res).catch(err => {
      console.log(`${'Error: '}${err}`); // eslint-disable-line
    });
  }
/* c8 ignore next 38 */
  buildNotification = (description, textContent, iconName, iconColor) => {
    const notification = this.createElement('eui-notification');
    notification.timeout = 5000;

    const icon = document.createElement('eui-icon');
    icon.size = '18px'
    icon.style = 'display: inline-block; vertical-align: middle;'

    notification.description = description;
    notification.textContent = textContent;
    icon.name = iconName
    icon.setAttribute('color', iconColor);

    notification.prepend(icon);

    // eslint-disable-line
    notification.addEventListener('click', event => { // eslint-disable-line
      console.log('clicked'); // eslint-disable-line
    });
    notification.showNotification();
  }
/* c8 ignore next 11 */
  _addNotification = (message) => {
    if (message === 'Enabled') {
      this.buildNotification("Application is currently Enabled. Please disable application to perform delete.", `${this.appName} failed to Delete`, 'cross', 'red' )
    } else if (message === 'Deleted') {
      this.buildNotification("Application and it\'s dependencies have been deleted", `${this.appName} successfully Deleted.`, 'check', 'green' )
    } else if (message === 'Deleting') {
      this.buildNotification("Application cannot be fully deleted, as there are active instances. Please disable all instances to delete", `${this.appName} is deleting`, 'triangle-warning', 'orange' )
    } else {
      this.buildNotification("Failure to Delete. Reason unknown! Please contact Administrator.", `${this.appName} failed to Delete`, 'cross', 'red' )
    }
  }
/* c8 ignore next 34 */
  _executeDeleteApp = () => { // eslint-disable-line
    if ((this.appStatus === "DEINITIALIZED" || this.appStatus === "CREATED") && this.appMode === "DISABLED") {
      this.deleteDialog.setAttribute('show', true);
    } else if (this.appStatus === "INITIALIZED" && this.appMode === "DISABLED") {
      this.initializedDialog.setAttribute('show', true)
    } else if (this.appStatus === "INITIALIZING" || this.appStatus === "DEINITIALIZING"  && this.appMode === "DISABLED") {
      this.initializingDialog.setAttribute('show', true)
    } else if (this.appStatus === "DELETE_ERROR" || this.appStatus === "INITIALIZE_ERROR" || this.appStatus === "DEINITIALIZE_ERROR"  && this.appMode === "DISABLED") {
      this.deleteErrorDialog.setAttribute('show', true)
    } else {
      this.initializedDialog.setAttribute('show', true)
    }
  };

  _deleteApp = () => {
    const getURL = `${restCalls.appLcm.deleteApp.request}${"/"}${getAppId()}`;
    fetch(`${getURL}`, {
      method: 'DELETE',
      headers: { "Content-Type": "application/json; charset=utf-8" },
    }).then((results) => {
      this.deleteDialog.removeAttribute('show');
      if (results.status === 204) {
        window.EUI.Router.goto('/#eric-oss-app-ui');
        this._addNotification('Deleted');
      } else if (results.status === 400) {
        this._addNotification('Enabled');
      } else {
        this._addNotification('Other');
      }
    })
    .catch(err => {
      console.info(`${'Error: '}${err}`); // eslint-disable-line
    });
  };

  get meta() {
    return import.meta;
  }

  _checkAccessPrivileges = async () => {
    return hasResource(REQUIRED_RESOURCE_V3_LCM, [GET]);
  }

  /**
   * Render the <e-eric-oss-app-ui> app. This function is called each time a
   * prop changes.
   */
  render() {
    const columns = [
      {
        title: 'Name', attribute: 'col1', width: '1em', sortable: true,
      /* c8 ignore next 32 */
        cell: (row, column)  => html`
        <div class='table__cell'>
          <eui-link href="#eric-oss-app-ui-app-artifacts?appid=${row[column.attribute].id}&component=${row[column.attribute].name}" subtle>${row[column.attribute].name}</eui-link>
        </div>`,
      },
      { title: 'Type', attribute: 'col2', width: '1em', sortable: true },
      { title: 'Version', attribute: 'col3', width: '1em', sortable: true },
      { title: 'Artifacts', attribute: 'col4', width: '2em', sortable: true },
    ];

    const accessDeniedDialog = html`
      <eui-dialog label="${this.i18n?.ACCESS_DENIED}" show noCancel>
        <div slot="content">
          ${this.i18n?.USER_ACCESS_ERROR}<br/><br/>
          ${this.i18n?.CONTACT_SYSTEM_ADMIN}
        </div>
        <p slot="cancel"></p>
        <eui-button slot="bottom" href="/#launcher" primary>${this.i18n?.RETURN_TO_LAUNCHER}</eui-button>
      </eui-dialog>
    `;

    const packageDetailsV2 =  html`
      <eui-tooltip id="initialize-app-btn-tooltip" position="left" delay="10" message="App is de-initialized and cannot be re-initialized." visible="${this.isDeinitTooltipVisible}">
        <eui-button id="initialize-app-btn" @click="${event => this._initializeDeInitializeApp(true)}" style="display:${this.canUserDelete ? "inline-block" : "none"};">Initialize App</eui-button>
      </eui-tooltip>
        <eui-button id="delete-app-btn" @click="${event => this._executeDeleteApp()}" style="display:${this.canUserDelete ? "inline-block" : "none"};">Delete App</eui-button>
       <div class="app-details__panel">
         <eui-card card-title="${this.appName}" --card-color-border="#767676">
         </eui-card>
         <div class="app-status__container">
           <div>
             <label>App Status <eui-info-popup message="App must be initialized" icon="triangle-warning" style="margin-left: 5px;"></eui-info-popup></label>
             <eui-switch data-cy="enable-disable__switch" @eui-switch:change="${event => this._updateMode(event.detail.on)}"
               label-on="Enabled" label-off="Disabled">
             </eui-switch>
           </div>
           <div>
             <label>Version</label>
             <eui-dropdown label="${this.appVersion}" data-type="single" data-cy="app-version__dropdown">
             </eui-dropdown>
           </div>
         </div>
       </div>
       <eui-tile tile-title="Components" subtitle="${this.lastUpdate}" class="artifacts__panel" data-cy="artifacts-panel">
         <div slot="content" data-cy="artifacts-table__content">
           <eui-table slot="content" .columns=${columns} .data=${this.appComponents} sortable data-cy="artifacts-table" .components=${{
            'eui-link': Link,
          }}>
           </eui-table>
         </div>
       </eui-tile>
       <eui-dialog label="Delete Application" data-cy="dialog-box" id="deleteDialog">
        <div slot="content">
          <p>This action will permanently delete the App Package and all contents.</p>
          <p>You will unable to interact with the application. Application will need to be onboarded again.</p>
          <p>Do you want to continue?</p>
        </div>
        <eui-button slot="bottom" warning data-cy="delete-btn"s
          @click="${event => this._deleteApp() /* eslint-disable-line */}">Delete</eui-button>
      </eui-dialog>
      <eui-dialog label="Initialized Application" data-cy="dialog-box" id="initializedDialog">
        <div slot="content">
          <p>Application cannot be deleted, as it is currently Initialized.</p>
          <p>De-initialize app to delete</p>
        </div>
      </eui-dialog>
      <eui-dialog label="Delete ERROR Application" data-cy="dialog-box" id="deleteErrorDialog">
        <div slot="content">
          <p>Application cannot be deleted, as it is currently in ERROR state.</p>
          <p>Please contact the Administrator.</p>
        </div>
      </eui-dialog>
      <eui-dialog label="Cannot Deinitialize an Application" data-cy="dialog-box" id="instantiatedDialog">
        <div slot="content">
          <p>Application cannot be Deinitialized, as it is currently in an instantiated / deployed state.</p>
          <p>Please contact the Administrator.</p>
        </div>
      </eui-dialog>
      <eui-dialog label="Deinitialize Application" data-cy="dialog-box" id="deinitializeDialog">
        <div slot="content">
          <p>Once this App is De-initialized it will enter Deinitialized state and cannot be re-initialized.</p>
        </div>
        <eui-button slot="bottom" warning data-cy="delete-btn"s
          @click="${event => this._initializeDeInitializeApp(false) /* eslint-disable-line */}">De-initialize</eui-button>
      </eui-dialog>
    `;

    return !this.canUserViewUI ? accessDeniedDialog : packageDetailsV2;
  }
}

definition('e-eric-oss-app-ui-package-details-v2', {
  style,
  props: {
    canUserDelete: { attribute: false, type: Boolean, default: true },
    canUserViewUI: { attribute: false, type: Boolean, default: true },
    appData: { attribute: false, type: Array, default: [] },
    appComponents: { attribute: false, type: Array, default: [] },
    isDeinitTooltipVisible: { attribute: false, type: String, default: 'never' },
    appVersion: { attribute: true, type: String, default: '' },
    appStatus: { attribute: true, type: String, default: '' },
    appName: { attribute: true, type: String, default: '' },
  },
})(EricOssAppUiPackageDetailsV2);

EricOssAppUiPackageDetailsV2.register();