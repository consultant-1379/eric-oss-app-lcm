import { restCallsV1 } from '../../config/restCallsConfigCypressV1';
import { restCalls } from '../../config/restCallsConfigCypress';

describe('Testing Package Details Page', () => {
  beforeEach(() => {
    const tableData = restCallsV1.onBoarding.getById;
    const permissionData = restCalls.permissions.onboardApp;
    cy.intercept(permissionData.request, { fixture: '18_onbBtnPermissionResponse.json' }).as('btnPermission');
    cy.intercept(tableData.request, { fixture: '02_onBoardingGetApp.json' }).as('appDetails')
    cy.visitPackageDetailsPageV1();
  })

  it('verify that app name is correctly displayed', () => {
    cy.wait('@appDetails').then(xhr => {
      cy.getPackageDetailsContainerV1()
        .find('eui-card').shadow()
        .find('.eui__card__title')
        .contains(xhr.response.body.name)
    })
  });

  it('verify that the app version is correctly populated in the dropdown', () => {
    cy.wait('@appDetails').then(xhr => {
      cy.getPackageDetailsContainerV1()
        .find('[data-cy="app-version__dropdown"]').shadow()
        .contains(xhr.response.body.version);
    });
  })

  it('verify that the artifacts are populated correctly in the table', () => {
    cy.wait('@appDetails').then(xhr => {
      cy.log('Checking the name is correctly populated');
      cy.getTableContainerV1().find('td').first().contains(xhr.response.body.artifacts[0].name);

      cy.log('Checking the type is correctly populated');
      cy.getTableContainerV1().contains(xhr.response.body.artifacts[0].type);

      cy.log('Checking the version is correctly populated');
      cy.getTableContainerV1().contains(xhr.response.body.artifacts[0].version);

      cy.log('Checking the app status is correctly populated');
      cy.getTableContainerV1().contains(xhr.response.body.artifacts[0].status);

      cy.log('Checking the app location is correctly populated');
      cy.getTableContainerV1().contains(xhr.response.body.artifacts[0].location);
    })
  });
})