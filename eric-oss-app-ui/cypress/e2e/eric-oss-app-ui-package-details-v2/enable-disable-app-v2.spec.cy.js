import { restCalls } from '../../config/restCallsConfigCypress';

describe('Enable Disable of an app', () => {
  beforeEach(() => {
    const tableData = restCalls.appLcm.getApp;
    const enableDisable = restCalls.appLcm.enableDisable;
    const permissionData = restCalls.permissions.onboardApp;
    cy.intercept(permissionData.request, { fixture: '18_onbBtnPermissionResponse.json' }).as('btnPermission');
    cy.intercept('GET', tableData.request, { fixture: '19_appLcmGetApp.json' }).as('appDetails');
    cy.intercept('PUT', enableDisable.request, { fixture: '19_appLcmGetApp.json' }).as('toggleStatus');
    cy.visitPackageDetailsPageV2();
  })

  it('verify that the app status toggle is present', () => {
    cy.getEnableDisableSwitchV2().shadow().find('.switch-label').contains("Disabled");
  });
})