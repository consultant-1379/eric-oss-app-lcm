import { restCalls } from '../../config/restCallsConfigCypress';


describe('Testing Onboard an App', () => {
  beforeEach(() => {
    const permissionData = restCalls.permissions.onboardApp;
    const tableData = restCalls.appLcm.getAllApps;
    cy.intercept(permissionData.request, { fixture: '18_onbBtnPermissionResponse.json' }).as('btnPermission');
    cy.intercept(tableData.request, { fixture: '18_appLCMGetAllApps.json' }).as('allApps');
    cy.visitAdminHomePage();

    cy.wait('@btnPermission').then(xhr => {
        cy.getApplicationLayerButton()
        .find('eui-button')
        .should('have.id', 'onboard-btn')
        .click();
    });
  })

  it('Verify "Onboarding An App" dialog correctly launches', () => {
    cy.getOnboardingDialog()
    .should('have.id', 'onboardDialog');
  });

  it('Verify CSAR file extension ONLY accepted', () => {
    cy.getOnboardingDialog()
    .find('[data-cy="onboard__fileInput"]')
    .invoke('attr', 'accept')
    .should('eq', '.csar');
  });

  it('Verify user input disabled for text field', () => {
    cy.getOnboardingDialog()
    .find('[data-cy="onboard__textField"]')
    .invoke('attr', 'disabled')
    .should('exist');
  });

  it('Verify onboarding button disabled while the text field is blank', () => {
    cy.getOnboardingDialog()
    .find('[data-cy="onboard__button"]')
    .invoke('attr', 'disabled')
    .should('exist');
  });

  it('Verify cancel button closes onboarding dialog', () => {
    cy.getOnboardingDialog()
    .shadow().find('eui-button')
    .contains('Cancel').click();
    cy.getOnboardingDialog().not();
  });
});