/**
 * EricOssAppUiPackageDetailsV1 is defined as
 * `<e-eric-oss-app-ui-package-details-v1>`
 *
 * Imperatively create application
 * @example
 * let app = new EricOssAppUiPackageDetailsV1();
 *
 * Declaratively create application
 * @example
 * <e-eric-oss-app-ui-package-details-v1></e-eric-oss-app-ui-package-details-v1>
 *
 * @extends {App, AppTable}
 */
import { App, html, definition } from '@eui/app';
import { Theme } from '@eui/theme';
import { Button, Switch, Dropdown } from '@eui/base';
import { Dialog } from '@eui/base/dialog';
import { Table } from "@eui/table";
import { Icon } from '@eui/theme/icon';
import { Tile } from '@eui/layout/tile';
import { Card } from '@eui/layout/card';
import { Link } from '@eui/base/link';
import { Banner } from '@eui/base/banner';
import style from './eric-oss-app-ui-package-details-v1.css';
import { restCallsV1, getAppIdV1 } from '../../config/restCallsConfigV1.js';
import DeleteAppV1 from '../../components/delete-app/delete-app-v1.js';
import { hasResource } from '../../utils/api/rbac';
import { GET } from "../../utils/constants"
import { REQUIRED_RESOURCE_V1_ONBOARDING } from "../../utils/resources";

export default class EricOssAppUiPackageDetailsV1 extends App {
  // Uncomment this block to add initialization code
  //  constructor() {
  //    super();
  //   // initialize
  //  }
  static get components() {
    return {
      // register components here
      'eui-icon': Icon,
      'eui-switch': Switch,
      'eui-dropdown': Dropdown,
      'eui-table': Table,
      'eui-tile': Tile,
      'eui-card': Card,
      'eui-dialog': Dialog,
      'eui-button': Button,
      'eui-banner': Banner,
      'eui-link': Link,
      'e-delete-app-v1': DeleteAppV1,
      'eui-theme': Theme,
    };
  }
  didUpgrade() {
    this.tableElement = this.shadowRoot.querySelector('eui-table');
    this.switchElement = this.shadowRoot.querySelector('eui-switch');
  }
  didConnect() {
    document.title = 'App Manager - Package Details';
    this._pollTableData();
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
            displayName: 'Package Details',
          },
        ],
    },);

    this._checkAccessPrivileges()
      .then(canViewUI => {
        if(!canViewUI){
          this.canUserViewUI = false;
        }
      })
      .catch(e => {
        console.error(e);
      });
  }
  /**
   * Updates the List of Apps asynchronously at 10 second intervals
   *
   * @function _pollTableData
   * @private
  */
  _pollTableData = async () => {
    const POLL_INTERVAL = 75000;
    try {
      await this._updateTableData();
      const getTime = new Date(Date.now()).toUTCString();
      this.lastUpdate = `Status Last Update: ${getTime}`;
      this.pollingTimer = setTimeout(this._pollTableData, POLL_INTERVAL);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.log(`Get list of Apps failed`, err);
    }
  }

  get meta() {
    return import.meta;
  }

  _checkAccessPrivileges = async () => {
    return hasResource(REQUIRED_RESOURCE_V1_ONBOARDING, GET);
  }

  _updateTableData = () => {
    this.artifactsData = [];
    this.applicationData = [];
    this.applicationName = '';
    this.applicationVersion = '';
    this.applicationEnabled = '';
    const getURL = `${restCallsV1.onBoarding.getAllApps.request}${"/"}${getAppIdV1()}`;
    fetch(`${getURL}`, { headers: { "Content-Type": "application/json; charset=utf-8" } })
      .then(res => res.json().then(artifacts => {
        const applicationData = artifacts;
        this.applicationData = [...this.applicationData, applicationData];
        artifacts.artifacts.map(artifact => { // eslint-disable-line
          const artifactDetails = {
            name: artifact.name,
            type: artifact.type,
            status: artifact.status,
            version: artifact.version,
            location: artifact.location,
          };
          this.artifactsData = [...this.artifactsData, artifactDetails];
          this.applicationName = this.applicationData[0].name;
          this.applicationVersion = this.applicationData[0].version;
          this.applicationEnabled = this.applicationData[0].mode;
          if (this.applicationEnabled === 'DISABLED') {
            this.switchElement.removeAttribute("on");
          } else {
            this.switchElement.setAttribute("on", true);
          };
        })
      }))
      .catch(err => {
        console.log(`${'Error: '}${err}`); // eslint-disable-line
      });
  }
  _updateMode = (mode) => { // eslint-disable-line
    !mode // eslint-disable-line
      ? this._enableDisable({ mode: 'DISABLED' })
      : this._enableDisable({ mode: 'ENABLED' })
  }
  _enableDisable = (enableDisable) => {
    const getURL = `${restCallsV1.onBoarding.getAllApps.request}${"/"}${getAppIdV1()}`;
    fetch(`${getURL}`, {
      method: 'PUT',
      headers: { "Content-Type": "application/json; charset=utf-8" },
      body: JSON.stringify(enableDisable),
    }).then(res => res).catch(err => {
      console.log(`${'Error: '}${err}`); // eslint-disable-line
    });
  }
  /**
   * Render the <e-eric-oss-app-ui> app. This function is called each time a
   * prop changes.
   */
  render() {
    const columns = [
      { title: 'Name', attribute: 'name', width: '1em', sortable: true },
      { title: 'Type', attribute: 'type', width: '1em', sortable: true },
      { title: 'Version', attribute: 'version', width: '1em', sortable: true },
      {
        title: 'Status', attribute: 'status', width: '1em', sortable: true,
        cell: (row) => row.status === "COMPLETED"
          ? html`<eui-icon name="check" color="green" style="vertical-align: middle;"></eui-icon> &nbsp;${row.status}`
          : html`<eui-icon name="cross" color="red" style="vertical-align: middle;"></eui-icon> &nbsp;${row.status}`,
      },
      { title: 'Location', attribute: 'location', width: '2em', sortable: true },
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

    const appDetailsElements = html`
      <e-delete-app-v1 application-name="${this.applicationName}"></e-delete-app-v1>
      <eui-banner id="v1-warning-banner" banner-type="persistent" color="orange" icon="triangle-warning" style="padding: 0px; margin: 0px">
        This App was onboarded using deprecated APIs. Please offboard and re-onboard this App. Refer to the product documentation for the deprecation statement.
      </eui-banner>
      <div class="app-details__panel">
        <eui-card card-title="${this.applicationName}" --card-color-border="#767676">
          <div slot="content">
            <eui-icon name="econ" size="52px"></eui-icon>
          </div>
        </eui-card>
        <div class="app-status__container">
          <div>
            <label>App Status</label>
            <eui-switch data-cy="enable-disable__switch" @eui-switch:change="${event => this._updateMode(event.detail.on)}"
              label-on="Enabled" label-off="Disabled">
            </eui-switch>
          </div>
          <div>
            <label>Version</label>
            <eui-dropdown label="${this.applicationVersion}" data-type="single" data-cy="app-version__dropdown">
            </eui-dropdown>
          </div>
        </div>
      </div>
      <eui-tile tile-title="Artifacts" subtitle="${this.lastUpdate}" class="artifacts__panel" data-cy="artifacts-panel">
        <div slot="content" data-cy="artifacts-table__content">
          <eui-table slot="content" .columns=${columns} .data=${this.artifactsData} sortable data-cy="artifacts-table">
          </eui-table>
        </div>
      </eui-tile>
    `;

    return !this.canUserViewUI ? accessDeniedDialog : appDetailsElements;
  }
}

definition('e-eric-oss-app-ui-package-details-v1', {
  style,
  props: {
    applicationData: { attribute: false, type: Array, default: [] },
    artifactsData: { attribute: false, type: Array, default: [] },
    applicationName: { attribute: true, type: String, default: '' },
    applicationVersion: { attribute: true, type: String, default: '' },
    applicationEnabled: { attribute: true, type: String, default: '' },
    canUserViewUI: { attribute: false, type: Boolean, default: true },
  },
})(EricOssAppUiPackageDetailsV1);
EricOssAppUiPackageDetailsV1.register();