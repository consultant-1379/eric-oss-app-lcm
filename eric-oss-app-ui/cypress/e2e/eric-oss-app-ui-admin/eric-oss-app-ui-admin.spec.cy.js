import { restCalls } from '../../config/restCallsConfigCypress';
import { restCallsV1 } from '../../config/restCallsConfigCypressV1';

describe('Testing Admin Page', () => {
  beforeEach(() => {
    const tableData = restCalls.appLcm.getAllApps;
    const permissionData = restCalls.permissions.onboardApp;
    cy.intercept(permissionData.request, { fixture: '18_onbBtnPermissionResponse.json' }).as('btnPermission');
    cy.intercept(tableData.request, { fixture: '18_appLCMGetAllApps.json' }).as('allApps');
    cy.visitAdminHomePage();
  })

  it('verify that the Onboarded Apps Table is populated correctly', () => {
    cy.wait('@allApps').then(xhr => {
      cy.getAdminHomeContainer()

      cy.log('Checking the name is correctly populated');
      cy.getOnboardingTableContainer().find('td').first().contains(xhr.response.body.items[0].name);

      let capitalize = (word) => {
        return word.charAt(0).toUpperCase() + word.substr(1).toLowerCase();
      }

      cy.log('Checking the status is correctly populated');
      let onboarded = capitalize(xhr.response.body.items[0].status);
      cy.getOnboardingTableContainer().contains(onboarded);

      cy.log('Checking the mode is correctly populated');
      let enabled = capitalize(xhr.response.body.items[0].mode);
      cy.getOnboardingTableContainer().contains(enabled);

      cy.log('Checking the date is correctly populated');
      cy.getOnboardingTableContainer().contains(new Date(xhr.response.body.items[0].createdAt).toUTCString());
    })
  });

  it('verify that app onboarded count is correctly displayed', () => {
    cy.wait('@allApps').then(xhr => {
      cy.getAdminContainer()
        .find('e-app-count').shadow()
        .find('[data-cy="apps_onboarded__count"]')
        .contains(6);
    })
  });

  it('verify that app enabled count is correctly displayed', () => {
    cy.wait('@allApps').then(xhr => {
      cy.getAdminContainer()
        .find('e-app-count').shadow()
        .find('[data-cy="apps_enabled__count"]')
        .contains(0);
    })
  });

  it('verify that onboarding jobs count is correctly displayed', () => {
    cy.wait('@allApps').then(xhr => {
      cy.getAdminContainer()
        .find('e-app-count').shadow()
        .find('[data-cy="jobs_onboarding__count"]')
        .contains("0");
    })
  });

  it('verify that onboarded apps tab is correctly displayed', () => {
    cy.wait('@allApps').then(xhr => {
      cy.getAdminContainer()
        .find('eui-tabs')
        .find('[data-cy="app_onboarded__tab"]')
        .contains("Onboarded Apps");
    })
  });

  it('verify that onboarded apps tab is correctly selected', () => {
    cy.wait('@allApps').then(xhr => {
      cy.getAdminContainer()
        .find('eui-tabs')
        .find('[data-cy="app_onboarded__tab"]').selected
    })
  });

  it('verify that deprecation banner is hidden when v1 apps are not found', () => {
    cy.wait('@allApps').then(xhr => {
      cy.getAdminContainer()
        .find('e-app-table').shadow()
        .find('eui-banner')
        .should('not.be.visible');
    })
  });

  it('verify that onboarding jobs tab is correctly displayed', () => {
    cy.wait('@allApps').then(xhr => {
      cy.getAdminContainer()
        .find('eui-tabs')
        .find('[data-cy="job_onboarding__tab"]')
        .contains("Onboarding Jobs");
    })
  });

  it('verify that Onboarding An App buttton is present', () => {
    cy.wait('@btnPermission').then(xhr => {
        cy.getApplicationLayerButton()
          .find('eui-button')
          .contains("Onboard an App");
     })
  });

  it('Verify theme is set to dark', () => {
    cy.getAdminHomeContainer()
    cy.get('body').should('have.css', 'background-color', 'rgb(36, 36, 36)');
    cy.get('body').should('have.css', 'color', 'rgb(242, 242, 242)');
  });

  it('verify that app onboarded count is correctly displayed when V1 apps are present', () => {
    const tableDataV1 = restCallsV1.onBoarding.getAllApps;
    cy.intercept(tableDataV1.request, { fixture: '01_onBoardingGetAllApps.json' }).as('allV1Apps');
    cy.wait(['@allApps', '@allV1Apps'],  {requestTimeout: 10000}).then(xhr => {
      cy.getAdminContainer()
        .find('e-app-count').shadow()
        .find('[data-cy="apps_onboarded__count"]')
        .contains(8);
    })
  });

  it('verify that deprecation banner is present when v1 apps are found', () => {
    const tableDataV1 = restCallsV1.onBoarding.getAllApps;
    cy.intercept(tableDataV1.request, { fixture: '01_onBoardingGetAllApps.json' }).as('allV1Apps');
    cy.wait(['@allApps', '@allV1Apps'],  {requestTimeout: 10000}).then(xhr => {
      cy.getAdminContainer()
        .find('e-app-table').shadow()
        .find('eui-banner')
        .should('be.visible');
    })
  });
})