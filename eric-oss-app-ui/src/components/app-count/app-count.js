/**
 * Component AppCount is defined as
 * `<e-app-count>`
 *
 * @extends {LitComponent}
 */
import { LitComponent, html, definition } from '@eui/lit-component';
import { Card } from '@eui/layout/card';
import style from './app-count.css';
import { restCalls } from '../../config/restCallsConfig.js';
import { restCallsV1 } from '../../config/restCallsConfigV1.js';

export default class AppCount extends LitComponent {
  // Uncomment this block to add initialization code
  constructor() {
    super();
    // initialize
  }

  static get components() {
    return {
      'eui-card': Card,
    };
  }

  didConnect() {
    this._pollAppCount();
  }

  didDisconnect() {
    clearTimeout(this.pollingTimer);
  }

  /**
   * Counts the number of Onboarded App, Enabled Apps, and Onboarding Jobs
   *
   * @function _updateTableData
   * @private
  */
  _updateCount = async () => {
    const getAllApps = restCalls.appLcm.getAllApps; // eslint-disable-line
    const getAllAppsV1 = restCallsV1.onBoarding.getAllApps; // eslint-disable-line
    const getAllJobs = restCalls.onBoarding.getOnboardingJobs; // eslint-disable-line

    // Get all v3 Apps Promise
    const fetchV3Apps = new Promise((resolve, reject) => {
      fetch(`${getAllApps.request}`, { headers: { "Content-Type": "application/json; charset=utf-8" } })
        .then(res => {
          if (res.status !== 200) {
            resolve({appCount: 0, enabledCount: 0});
          }
          return res.json();
        })
        .then(response => {
          let appCount = response.items.length;
          let enabledCount = 0;

          response.items.map(app => {
            if (app.mode === 'ENABLED') { enabledCount++; } // eslint-disable-line
          });

          resolve({appCount: appCount, enabledCount: enabledCount});
        })
        .catch(err => {
          console.log(`${'Error: '}${err}`) // eslint-disable-line
          resolve({appCount: 0, enabledCount: 0});
        });
    })

    // Get all v1 Apps promise
    const fetchV1Apps = new Promise((resolve, reject) => {
      fetch(`${getAllAppsV1.request}`, { headers: { "Content-Type": "application/json; charset=utf-8" } })
        .then(res => {
          if (res.status !== 200) {
            resolve({appCount: 0, enabledCount: 0});
          }
          return res.json()
        })
        .then(response => {
          let appCount = response.length;
          let enabledCount = 0;

          response.map(app => {
            if (app.mode === 'ENABLED') { enabledCount++; } // eslint-disable-line
          });

          resolve({appCount: appCount, enabledCount: enabledCount});
        })
        .catch(err => {
          console.log(`${'Error: '}${err}`); // eslint-disable-line
          resolve({appCount: 0, enabledCount: 0});
        })
    })

    const fetchV2Jobs = new Promise((resolve, reject) => {
      fetch(`${getAllJobs.request}`, { headers: { "Content-Type": "application/json; charset=utf-8" } })
        .then(res => {
          if (res.status !== 200) {
            resolve({appCount: 0, enabledCount: 0});
          }
          return res.json();
        })
        .then(response => {
          resolve({jobsCount: response.items.length});
        })
        .catch(err => {
          console.log(`${'Error: '}${err}`); // eslint-disable-line
          resolve({jobsCount: 0});
        })
    });

    Promise.all([fetchV1Apps, fetchV3Apps, fetchV2Jobs]).then(responses => {
      this.onboardedCount = 0;
      this.enabledCount = 0;
      this.jobsCount = 0;

      for (const response of responses) {
        if (response.jobsCount) { this.jobsCount += response.jobsCount; };  // eslint-disable-line
        if (response.appCount) { this.onboardedCount += response.appCount; };  // eslint-disable-line
        if (response.enabledCount) { this.enabledCount += response.enabledCount; };  // eslint-disable-line
      }
    })
  }

  /**
   * Polls the App counts asynchronously at 10 second intervals
   *
   * @function _pollAppCount
   * @private
  */
  _pollAppCount = async () => {
    const POLL_INTERVAL = 10000;
    try {
      await this._updateCount();

      this.pollingTimer = setTimeout(this._pollAppCount, POLL_INTERVAL);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.log(`Get App Count failed`, err);
    }
  }

  render() {
    return html`
      <div class="app__count__container">
        <div>
          <label>Apps Onboarded</label>
          <div data-cy="apps_onboarded__count" class="app__count" slot="content">${this.onboardedCount}</div>
        </div>
        <div>
          <label>Apps Enabled</label>
          <div data-cy="apps_enabled__count" class="app__count" slot="content">${this.enabledCount}</div>
        </div>
        <div>
          <label>Onboarding Jobs</label>
          <div data-cy="jobs_onboarding__count" class="app__count" slot="content">${this.jobsCount}</div>
        </div>
      </div>
      `;
  }
}

definition('e-app-count', {
  style,
  props: {
    onboardedCount: { attribute: true, type: Number, default: 0 },
    enabledCount: { attribute: true, type: Number, default: 0 },
    jobsCount: { attribute: true, type: Number, default: 0 },
    pollingTimer: { type: Number, attribute: false },
  },
})(AppCount);