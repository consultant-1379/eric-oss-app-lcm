/**
 * Component AppTable is defined as
 * `<e-app-table>`
 *
 * @extends {LitComponent}
*/
import { LitComponent, html, definition } from '@eui/lit-component';
import { Table } from '@eui/table';
import { Tile } from '@eui/layout/tile';
import { Link } from '@eui/base/link';
import { Loader } from '@eui/base/loader';
import { Banner } from '@eui/base/banner';
import { Tooltip } from '@eui/base/tooltip';
import style from './app-table.css';
import { restCalls } from '../../config/restCallsConfig.js';
import { restCallsV1 } from '../../config/restCallsConfigV1.js';

export default class AppTable extends LitComponent {
  constructor() {
    super();
    Table.register();
    this.tableElement = null;
  }

  static get components() {
    return {
      'eui-table': Table,
      'eui-tile': Tile,
      'eui-banner': Banner,
      'eui-link': Link,
      'eui-tooltip': Tooltip,
    };
  }

  didUpgrade() {
    this.tableElement = this.shadowRoot.querySelector('eui-table');
  }

  didConnect() {
    this._pollTableData();
  }

  didDisconnect() {
    clearTimeout(this.pollingTimer);
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
      this.lastAppUpdate = `Last Update: ${getTime}`;

      this.pollingTimer = setTimeout(this._pollTableData, POLL_INTERVAL);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.log(`Get list of Apps failed`, err);
    }
  }

  /**
   * Populates table output with Apps
   *
   * @function _updateTableData
   * @private
  */
  _updateTableData = async () => {
    const getApps = restCalls.appLcm.getAllApps;
    const getAppsV1 = restCallsV1.onBoarding.getAllApps;

    // Get V3 Apps Promise
    const fetchV3Apps = new Promise((resolve, reject) => {
      fetch(`${getApps.request}`, { headers: { "Content-Type": "application/json; charset=utf-8" } })
        .then(res => {
          if (res.status !== 200) {
            resolve({appData: []});
          }
          return res.json();
        })
        .then(response => {
          let v3Apps = [];

          response.items.map(app => {
            const newRow = {
              col1: { id: app.id, name: app.name, href: `${'#eric-oss-app-ui-package-details-v2?appid='}${app.id}` },
              col2: app.id,
              col3: app.version,
              col4: { id: app.id, status: app.status, isDeinit: app.status == "DEINITIALIZED" ? true : false },
              col5: this._toTitleCase(app.mode),
              col6: new Date(app.createdAt).toUTCString(),
            }
            v3Apps.push(newRow);
          });
          resolve({appData: v3Apps});
        })
        .catch(err => {
          console.log(`${'Error: '}${err}`) // eslint-disable-line
          resolve({appData: []});
        });
    })

    // Get V1 Apps Promise
    const fetchV1Apps = new Promise((resolve, reject) => {
      fetch(`${getAppsV1.request}`, { headers: { "Content-Type": "application/json; charset=utf-8" } })
        .then(res => {
          if (res.status !== 200) {
            resolve({banner: "none"});
          }
          return res.json();
        })
        .then(response => {
          let v1Apps = [];
          let bannerShow = "none";

          response.map(app => {
            bannerShow = "block";

            const newRow = {
              col1: { id: app.id, name: app.name, href: `${'#eric-oss-app-ui-package-details-v1?appid='}${app.id}`, v1: true },
              col2: app.id,
              col3: app.version,
              col4: { id: app.id, status: app.status },
              col5: this._toTitleCase(app.mode),
              col6: new Date(Date.parse(app.onboardedDate)).toUTCString(),
            }
            v1Apps.push(newRow);
          });
          resolve({appData: v1Apps, banner: bannerShow})
        })
        .catch(err => {
          console.log(`${'Error: '}${err}`) // eslint-disable-line
          resolve({banner: "none"});
        });
    });

    // Wait for both V1Apps and V3Apps promises to resolve before updating table
    Promise.all([fetchV1Apps, fetchV3Apps]).then(responses => {
      this.data = [];

      for (const response of responses) {
        if (response.appData) {
          this.data.push(...response.appData);
        }

        if (response.banner) {
          this.v1BannerShow = response.banner;
        }
      }
      this.data.sort((x, y) => {
        return (Date.parse(x.col6) < Date.parse(y.col6)) ? 1 : -1;
      })
    })
  }

  /**
   * Coverts input words to title case
   *
   * @function _toTitleCase
   * @private
  */
  _toTitleCase = (str) => { // eslint-disable-line
    return str.replace(/\w\S*/g, function (txt) { // eslint-disable-line
      return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase(); // eslint-disable-line
    }, // eslint-disable-line
    ); // eslint-disable-line
  } // eslint-disable-line

  render() {
    return html`
      <div class="app__table__container">
        <eui-banner id="v1-warning-banner" banner-type="persistent" color="orange" icon="triangle-warning" style="display:${this.v1BannerShow}">
          Some Apps were onboarded using deprecated APIs. Please offboard and re-onboard these Apps. Refer to the product documentation for the deprecation statement.
        </eui-banner>
        <eui-tile tile-title="List of Created Apps" subtitle="${this.lastAppUpdate}" column=0>
          <eui-table data-cy="onboarded_apps__table" slot="content" class='apps__list' .components=${{ 'eui-link': Link, 'eui-loader': Loader, 'eui-tooltip': Tooltip }} .columns=${this.columns} .data=${this.data}  ?sortable=${true} ?resizable=${true}></eui-table>
        </eui-tile>
      </div>
    `;
  }
}

definition('e-app-table', {
  style,
  props: {
    columns: {
      attribute: false, type: Array, default: [
        {
          title: 'App Name',
        /* c8 ignore next 5 */
          cell: (row, column) => html`
        <div class='table__cell' style="justify-content:space-between">
          <eui-link id="app${row[column.attribute].id}" href=${row[column.attribute].href} subtle>${row[column.attribute].name}
          </eui-link>
          <eui-tooltip style="display:${row[column.attribute].v1 ? "block" : "none"};" position="right" delay="10" message="App is running on an older version and needs to be re-onboarded"><eui-icon color="orange" name="triangle-warning"></eui-icon></eui-tooltip>
        </div>
       `,
       /* c8 ignore next 9 */
          sortType: (rowA, rowB, column) => {
            if (rowA[column.attribute].name < rowB[column.attribute].name) {
              return column.sort === 'asc' ? -1 : 1;
            }
            if (rowA[column.attribute].name > rowB[column.attribute].name) {
              return column.sort === 'asc' ? 1 : -1;
            }
            return 0;
          },
          attribute: 'col1', "sortable": true, width: "auto"
        },
        { title: 'App ID', attribute: 'col2', "sortable": true, width: "auto"  },
        { title: 'Version', attribute: 'col3', "sortable": true, width: "auto"  },
        {
          title: 'App Status',
        /* c8 ignore next 27 */
          cell: (row, column) => {
            if ((row[column.attribute].status === "CREATED") || (row[column.attribute].status === "INITIALIZED") || (row[column.attribute].status === "ONBOARDED")) {
              return html`
            <div id="status${row[column.attribute].id}" class='table__cell'>
              <eui-icon color="green" name="check"></eui-icon>
              &nbsp;&nbsp;${row[column.attribute].status.toLowerCase().replace(/\b\w/g, s => s.toUpperCase())}
            </div>`
            } else if ((row[column.attribute].status === "INITIALIZING") || (row[column.attribute].status === "DEINITIALIZING")) { // eslint-disable-line
              return html`
            <div id="status${row[column.attribute].id}" class='table__cell'>
              <eui-loader size="small"></eui-loader>
              &nbsp;&nbsp;${row[column.attribute].status.toLowerCase().replace(/\b\w/g, s => s.toUpperCase())}
            </div>`
            } else if ((row[column.attribute].status === "DELETE_ERROR") || (row[column.attribute].status === "INITIALIZE_ERROR") || (row[column.attribute].status === "DEINITIALIZE_ERROR") || (row[column.attribute].status === "FAILED")) {
              return html`
            <div id="status${row[column.attribute].id}" class='table__cell'>
            <eui-icon color="red" name="cross"></eui-icon>
              &nbsp;&nbsp;${row[column.attribute].status.toLowerCase().replace(/\b\w/g, s => s.toUpperCase())}
            </div>`
            } else if ((row[column.attribute].status === "DEINITIALIZED")) {
              return html`
                <div id="status${row[column.attribute].id}" class='table__cell' style="justify-content:space-between">
                  &nbsp;&nbsp;${row[column.attribute].status.toLowerCase().replace(/\b\w/g, s => s.toUpperCase())}
                  <eui-tooltip style="display:${row[column.attribute].isDeinit ? "block" : "none"};" position="right" delay="10" message="App is de-initialized and cannot be re-initialized."><eui-icon color="white" name="info"></eui-icon></eui-tooltip>
                </div>`
            } else {
              return html`
            <div id="status${row[column.attribute].id}" class='table__cell'>
              <eui-icon color="green" name="check"></eui-icon>
              &nbsp;&nbsp;${row[column.attribute].status}
            </div>`
            }
          },
          attribute: 'col4', "sortable": true, width: "auto"
        },
        { title: 'Admin Status', attribute: 'col5', "sortable": true, width: "auto" },
        { title: 'Created Date', attribute: 'col6', "sortable": true, width: "auto" },
      ],
    },
    data: { attribute: false, type: Array, default: [] },
    pollingTimer: { type: Number, attribute: false },
    lastAppUpdate: { attribute: false },
    v1BannerShow: { attribute: false, default: "none" },
  },
})(AppTable);

AppTable.register();