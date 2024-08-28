/**
 * Component JobTable is defined as
 * `<e-job-table>`
 *
 * @extends {LitComponent}
 */
import { LitComponent, html, definition } from '@eui/lit-component';
import { Button, Tooltip, Dialog } from '@eui/base';
import { Icon, Theme } from '@eui/theme';
import { Setting, Table } from '@eui/table';
import { Tile } from '@eui/layout/tile';
import { Menu, MenuItem, Notification } from '@eui/base';
import { Loader } from '@eui/base/loader';
import { restCalls } from '../../config/restCallsConfig.js';
import style from './job-table.css';

export default class JobTable extends LitComponent {
  constructor() {
    super();
    Tile.register();
    Button.register();
    Dialog.register();
    Tooltip.register();
    Table.register();
    Setting.register();
    Loader.register();
    this.tableElement = null;
  }

  static get components() {
    return {
      'eui-button': Button,
      'eui-table': Table,
      'eui-table-setting': Setting,
      'eui-tile': Tile,
      'eui-menu': Menu,
      'eui-dialog': Dialog,
      'eui-menu-item': MenuItem,
      'eui-notification': Notification,
      'eui-theme': Theme,
      'eui-icon': Icon,
      'eui-loader': Loader,
      'eui-tooltip': Tooltip,
    };
  }

  didUpgrade() {
    this.jobDeleteDialog = this.shadowRoot.querySelector('eui-dialog');
    this.tableElement = this.shadowRoot.querySelector('eui-table');
    this.tableSetting = this.shadowRoot.querySelector('eui-table-setting');
    this.deleteButton = this.shadowRoot.querySelector('#delete');
  }

  didConnect() {
    this._pollJobTable();
  }

  didDisconnect() {
    clearTimeout(this.pollingTimer);
  }

  /* c8 ignore next 22 */
  _toggleJobDialog = () => {
    this._showDelete = !this._showDelete;
  };

  /**
   * Called whenever a row in the table is selected.
   *
   * @function _rowSelected
   * @param event The row selected event.
   * @private
   */
  _rowSelected = async (event) => {
    console.log(event.detail);
    if (event.detail.selected === true) {
      this.deleteButton.removeAttribute('disabled');
      this.rowSelectId = event.detail.col6;
    } else if (event.detail.selected === false){
      this.deleteButton.setAttribute('disabled', true);
    }

    this.bubble(
      `e-job-table:${this.context}:row-selected`,
      event.detail,
    );
  }

  handleEvent(event) {
    if (event.type === 'click' && event.target.id === 'delete') {
      this._showDelete = !this._showDelete;
    }
    if (event.type === 'click' && event.target.id === 'settings') {
      //set the table settings columns
      this.tableSetting.columns = this.columns;
      // open/close dialog...
      this._showSettings = !this._showSettings;
    }
    if (event.type === 'click' && event.target.id === 'apply-settings') {
      // call apply on the table settings component...
      this.tableSetting.apply();
    }
    if (event.type === 'eui-table-setting:apply') {
      // apply the settings...
      this.columns = event.detail.value;
      // close dialog...
      this._showSettings = false;
    }
    if (event.type === 'eui-dialog:cancel') {
      // call cancel on the table settings component...
      this.tableSetting.cancel();
      this._showSettings = false;
      this._showDelete = false;
    }
  }

  /**
   * Populates Onboarding Jobs output with list of jobs
   *
   * @function _updateJobsTable
   * @private
  */
  _updateJobsTable = async () => {
    const getJobs = restCalls.onBoarding.getOnboardingJobs;
    fetch(`${getJobs.request}`, { headers: { "Content-Type": "application/json; charset=utf-8" } })
      .then(res => res.json())
      .then(response => {
        this.jobs = [];

        for (let items in response) {
          response[items].map((job) => {
            const newRow = {
              col1: job.fileName,
              col2: job.packageVersion,
              col3: job.packageSize,
              col4: job.vendor,
              col5: job.app.id,
              col6: job.id,
              col7: { status: job.status },
              col8: new Date(job.onboardStartedAt).toUTCString(),
              col9: new Date(job.onboardEndedAt).toUTCString(),
            }
            this.jobs.unshift(newRow);
          });
        }
      })
      .catch(err => {
        console.error(`${'Error: '}${err}`) // eslint-disable-line
      });
  }

  /**
   * Updates the List of Onboarding Jobs asynchronously at 10 second intervals
   *
   * @function _pollJobTable
   * @private
  */
  _pollJobTable = async () => {
    const POLL_INTERVAL = 10000;
    try {
      await this._updateJobsTable();

      const getTime = new Date(Date.now()).toUTCString();
      this.lastJobUpdate = `Last Update: ${getTime}`;

      this.pollingTimer = setTimeout(this._pollJobTable, POLL_INTERVAL);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error(`Get list of Onboarding Jobs failed`, err);
    }
  }

  /* c8 ignore next 19 */
  /**
   * Fires a notification to confirm the deletion of a job
   *
   * @function _deleteJobs
   * @private
  */
  _deleteNotification = (status) => {
    const notification = this.createElement('eui-notification');
    notification.timeout = 10000;
    const notificationIcon = document.createElement('eui-icon');
    notificationIcon.size = '18px';
    notificationIcon.style = 'display: inline-block; vertical-align: middle;';
    notification.textContent = 'Delete Job';
    if (status === 'SUCCESS') {
      notificationIcon.name = 'check';
      notificationIcon.setAttribute('color', 'green');
      notification.description = "Successfully deleted selected job";
    }
    else if (status === 'FAILED') {
      notificationIcon.name = 'cross';
      notificationIcon.setAttribute('color', 'red');
      notification.description = "Failed to delete the selected job";
    }
    notification.prepend(notificationIcon);
    notification.showNotification();
  };

  /* c8 ignore next 24 */
  /**
   * Deletes the selected job from the list of onboarding jobs table
   *
   * @function _deleteJobs
   * @private
  */
  _deleteJobs = async () => {
    let promises = [];
    const deleteJob = restCalls.onBoarding.deleteOnboardingJob;
    promises.push(
      fetch(`${deleteJob.request}/${this.rowSelectId}`, {
        method: 'DELETE',
        headers: { "Content-Type": "application/json; charset=utf-8" },
      }).then(response => {
        this._showDelete = !this._showDelete;
        if (response.status === 204) {
          this.deleteButton.setAttribute('disabled', true);
          this._deleteNotification('SUCCESS');
        }
        else {
          this._deleteNotification('FAILED');
        }
      })
        .catch(err => {
          console.error(`${'Error: '}${err}`) // eslint-disable-line
        })
    );
    Promise.all(promises).then(response => {
      this._updateJobsTable();
    });
  }

  render() {
    return html`
      <div class="job__table__container">
        <eui-tile tile-title="List of Onboarding Jobs" subtitle="${this.lastJobUpdate}"  column=0>
          <eui-table .data=${this.jobs} .columns=${this.columns} @eui-table:row-click=${event => this._rowSelected(event)} slot="content" ?resizable=${true} single-select virtual-scroll></eui-table>
          <eui-button data-cy="delete_job__button" id="delete" @click="${e => this._toggleJobDialog(e)}" slot="action" secondary disabled>Delete Job</eui-button>
        </eui-tile>
      </div>
      <eui-dialog label="Delete Job(s)" ?show=${this._showDelete} @eui-dialog:cancel=${this}>
        <div slot="content">Are you sure you want to delete the selected job?</div>
        <eui-button slot="bottom" @click="${e => this._deleteJobs(e)}" warning>Delete</eui-button>
      </eui-dialog>
    `;
  }
}

definition('e-job-table', {
  style,
  props: {
    columns: {
      type: Array, default: [
        { title: 'Package Name', attribute: 'col1', width: "auto" },
        { title: 'Package Version', attribute: 'col2', width: "auto" },
        { title: 'Size', attribute: 'col3', width: "auto" },
        { title: 'Vendor', attribute: 'col4', width: "auto" },
        { title: 'App ID', attribute: 'col5', width: "auto" },
        { title: 'Job ID', attribute: 'col6', width: "auto" },
        {
          title: 'Job Status',
          /* c8 ignore next 27 */
          cell: (row, column) => {
            if ((row[column.attribute].status === "ONBOARDED")) {
              return html`
            <div id="status${row[column.attribute].id}" class='table__cell'>
              <eui-icon color="green" name="check"></eui-icon>
              &nbsp;&nbsp;${row[column.attribute].status.toLowerCase().replace(/\b\w/g, s => s.toUpperCase())}
            </div>`
            } else if ((row[column.attribute].status === "UPLOADED") || (row[column.attribute].status === "PARSED") || (row[column.attribute].status === "UNPACKED")) { // eslint-disable-line
              return html`
            <div id="status${row[column.attribute].id}" class='table__cell'>
              <eui-loader size="small"></eui-loader>
              &nbsp;&nbsp;${row[column.attribute].status.toLowerCase().replace(/\b\w/g, s => s.toUpperCase())}
            </div>`
            } else if ((row[column.attribute].status === "ROLLBACK_FAILED") || (row[column.attribute].status === "FAILED")) {
              return html`
            <div id="status${row[column.attribute].id}" class='table__cell'>
            <eui-icon color="red" name="cross"></eui-icon>
              &nbsp;&nbsp;${row[column.attribute].status.toLowerCase().replace(/\b\w/g, s => s.toUpperCase())}
            </div>`
            } else {
              return html`
            <div id="status${row[column.attribute].id}" class='table__cell'>
              <eui-icon color="green" name="check"></eui-icon>
              &nbsp;&nbsp;${row[column.attribute].status}
            </div>`
            }
          },
          attribute: 'col7', width: "auto"
        },
        { title: 'Start Time', attribute: 'col8', width: "auto" },
        { title: 'End Time', attribute: 'col9', width: "auto" },
      ],
    },
    jobs: { type: Array },
    pollingTimer: { type: Number },
    _showSettings: { type: Boolean },
    _showDelete: { type: Boolean }
  },
})(JobTable);

JobTable.register();