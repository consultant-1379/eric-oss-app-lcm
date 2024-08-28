import { restCalls } from '../../config/restCallsConfigCypress';

context('Delete an App', () => {
  beforeEach(() => {
    const permissionData = restCalls.permissions.onboardApp;
    cy.intercept(permissionData.request, { fixture: '18_onbBtnPermissionResponse.json' }).as('btnPermission');
    cy.visitPackageDetailsPageV1();
  })

  it('verify that delete app button is clicked and dialog box is opened', () => {
    cy.wait('@btnPermission').then(xhr => {
      cy.log(`*** Clicking Delete app button and the dialog box pops up ***`);
      cy.getDeleteAppButtonV1();
      cy.log(`*** Clicking on Cancel button to close the dialog box ***`);
      cy.getDeleteDialogBoxV1()
        .shadow().find('eui-button')
        .contains('Cancel').click();
    });
  })
})
