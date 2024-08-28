/**
 * EricOssAppUiAppArtifacts is defined as
 * `<e-eric-oss-app-ui-app-artifacts>`
 *
 * @extends {App}
 */
import { App, html, definition } from '@eui/app';
import { Button } from '@eui/base';
import { Tile } from '@eui/layout/tile';
import { Table } from "@eui/table";
import { Dialog } from '@eui/base/dialog';
import { restCalls, getAppIdForArtifacts, getComponentName } from '../../config/restCallsConfig.js';
import style from './eric-oss-app-ui-app-artifacts.css';
import { hasResource } from '../../utils/api/rbac';
import { GET } from "../../utils/constants"
import { REQUIRED_RESOURCE_V3_LCM } from "../../utils/resources";

export default class EricOssAppUiAppArtifacts extends App {
  constructor() {
     super();
     Table.register();
  }

  static get components() {
    return {
      'eui-table': Table,
      'eui-tile': Tile,
      'eui-button': Button,
      'eui-dialog': Dialog,
    };
  }

  didConnect() {
    this.bubble('app:title', { displayName: 'App Artifacts' });
    this.componentName = getComponentName();
    this._updateTableData();

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

  _createBreadcrumb = (applicationId) => {
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
            action: () => { window.EUI.Router.goto(`/#eric-oss-app-ui-package-details-v2?appid=${applicationId}`) },
          },
          {
            displayName: 'App Artifacts',
          },
        ],
      },
    );
  }

  _updateTableData = () => {
    this.artifactsData = [];
    const getURL = `${restCalls.appLcm.getAllApps.request}${"/"}${getAppIdForArtifacts()}`;
      fetch(`${getURL}`, { headers: { "Content-Type": "application/json; charset=utf-8" } })
        .then(res => res.json())
        .then(app => {
          this.appId = app.id;
          this.appData = [...this.appData, app];
          this.appName = app.name;
          this.appStatus = app.status

          this.appComponents = [];
          for (let component in app.components) {
            if(app.components[component].name === this.componentName) {
              for(let artifact in app.components[component].artifacts) {
                const artifactDetails = {
                  col1: app.components[component].artifacts[artifact].name,
                  col2: app.components[component].artifacts[artifact].type,
                  col3: this.appStatus,
                  col4: app.components[component].artifacts[artifact].location
                }
                this.artifactsData = [...this.artifactsData, artifactDetails];
              }
            }
          }
          this.appName = app.name;
          this._createBreadcrumb(this.appId);
        })
        .catch(err => {
          console.log(`${'Error: '}${err}`); // eslint-disable-line
        });
  }

  get meta() {
    return import.meta;
  }

  _checkAccessPrivileges = async () => {
    return hasResource(REQUIRED_RESOURCE_V3_LCM, [GET]);
  }

  /**
   * Render the <e-eric-oss-app-ui-app-artifacts> app. This function is called each time a
   * prop changes.
   */
  render() {
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

    const artifactsPage = html`
      <div class="app__table__container">
        <eui-tile tile-title="Artifacts" subtitle=${this.componentName} column=0>
          <eui-table data-cy="onboarded_apps__table" slot="content" .columns=${this.columns} .data=${this.artifactsData} ?sortable=${true} resizable></eui-table>
        </eui-tile>
      </div>
    `;

    return !this.canUserViewUI ? accessDeniedDialog : artifactsPage;
  }
}

definition('e-eric-oss-app-ui-app-artifacts', {
  style,
  props: {
    columns: {
      attribute: false, type: Array, default: [
        { title: 'Name', attribute: 'col1', "sortable": true, width: 'auto' },
        { title: 'Type', attribute: 'col2', "sortable": true, width: 'auto' },
        { title: 'Status', attribute: 'col3', "sortable": true, width: 'auto' },
        { title: 'Relative URI', attribute: 'col4', "sortable": true, width: 'auto' },
      ],
    },
    canUserViewUI: { attribute: false, type: Boolean, default: true },
    data: { attribute: false, type: Array, default: [] },
    appData: { attribute: false, type: Array, default: [] },
    artifactsData: { attribute: false, type: Array, default: [] },
    appName: { attribute: true, type: String, default: '' },
  },
})(EricOssAppUiAppArtifacts);

EricOssAppUiAppArtifacts.register();
