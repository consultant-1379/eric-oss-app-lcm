/**
 * EricOssAppUi is defined as
 * `<e-eric-oss-app-ui>`
 *
 * Imperatively create application
 * @example
 * let app = new EricOssAppUi();
 *
 * Declaratively create application
 * @example
 * <e-eric-oss-app-ui></e-eric-oss-app-ui>
 *
 * @extends {App, AppTable}
 */
import { Button, TextField } from '@eui/base';
import { Theme } from '@eui/theme';
import { App, html, definition } from '@eui/app';
import { Tab, Tabs } from '@eui/layout';
import { Dialog } from '@eui/base/dialog';
import AppCount from '../../components/app-count/app-count.js';
import AppTable from '../../components/app-table/app-table.js';
import JobTable from '../../components/job-table/job-table.js';
import AppOnboard from '../../components/app-onboard/app-onboard.js';
import style from './eric-oss-app-ui.css';
import { hasResource } from '../../utils/api/rbac';
import { GET } from "../../utils/constants"
import { REQUIRED_RESOURCE_V1_ONBOARDING, REQUIRED_RESOURCE_V3_LCM } from "../../utils/resources";

export default class EricOssAppUi extends App {
  static get components() {
    return {
      'eui-button': Button,
      'eui-text-field': TextField,
      'eui-tabs': Tabs,
      'eui-tab': Tab,
      'eui-dialog': Dialog,
      'e-app-count': AppCount,
      'e-app-table': AppTable,
      'e-job-table': JobTable,
      'e-app-onboard': AppOnboard,
      'eui-theme': Theme,
    };
  }

  didConnect() {
    document.title = 'App Manager - Admin';
    this.bubble('app:breadcrumb', {
      breadcrumb:
      [
        {
          displayName: 'EIC',
        },
        {
          displayName: 'App Administration',
        },
      ],
    });

    this._checkUserPermissions()
      .then(canViewUI => {
        if(!canViewUI){
          this.canUserViewUI = false;
        }
      })
      .catch(e => {
        console.error(e);
      });
  }

  get meta() {
    return import.meta;
  }

  _checkUserPermissions = async () => {
    let resource1 = await hasResource(REQUIRED_RESOURCE_V3_LCM, [GET]);
    let resource2 = await hasResource(REQUIRED_RESOURCE_V1_ONBOARDING, [GET]);

    return resource1 || resource2;
  }

  render()  {
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

    const appAdminUIElements = html`
      <e-app-onboard></e-app-onboard>
      <div class="layout__container">
        <div class="layout__row">
          <e-app-count></e-app-count>
        </div>
        <div class="layout__row">
          <eui-tabs>
            <eui-tab data-cy="app_onboarded__tab" selected>Onboarded Apps</eui-tab>
            <eui-tab data-cy="job_onboarding__tab">Onboarding Jobs</eui-tab>

            <e-app-table slot="content"></e-app-table>
            <e-job-table slot="content"></e-job-table>
          </eui-tabs>
        </div>
      </div>
    `;

    return !this.canUserViewUI ? accessDeniedDialog : appAdminUIElements;
  }
}

definition('e-eric-oss-app-ui', {
  style,
  props: {
    canUserViewUI: { attribute: false, type: Boolean, default: true },
  },
})(EricOssAppUi);

EricOssAppUi.register();