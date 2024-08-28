import { restCalls } from '../../config/restCallsConfigCypress';

describe('Testing Package Details Page', () => {
  beforeEach(() => {
    const tableData = restCalls.appLcm.getApp;
    const permissionData = restCalls.permissions.onboardApp;
    cy.intercept(permissionData.request, { fixture: '18_onbBtnPermissionResponse.json' }).as('btnPermission');
    cy.intercept(tableData.request, { fixture: '19_appLcmGetApp.json' }).as('appDetails')
    cy.visitPackageDetailsPageV2();
  })

  it('verify that app name is correctly displayed', () => {
    cy.wait('@appDetails').then(xhr => {
      cy.getPackageDetailsContainerV2()
        .find('eui-card').shadow()
        .find('.eui__card__title')
        .contains(xhr.response.body.name)
    })
  });

  it('verify that the app version is correctly populated in the dropdown', () => {
    cy.wait('@appDetails').then(xhr => {
      cy.getPackageDetailsContainerV2()
        .find('[data-cy="app-version__dropdown"]').shadow()
        .contains(xhr.response.body.version);
    });
  })

  it('verify that the components are populated correctly in the table', () => {
    cy.wait('@appDetails').then(xhr => {
      console.log(xhr.response.body);
      cy.log('Checking the name is correctly populated');
      cy.getTableContainerV2().find('td').first().contains(xhr.response.body.components[0].name);

      cy.log('Checking the type is correctly populated');
      cy.getTableContainerV2().contains(xhr.response.body.components[0].type);

      cy.log('Checking the version is correctly populated');
      cy.getTableContainerV2().contains(xhr.response.body.components[0].version);
    })
  });
})