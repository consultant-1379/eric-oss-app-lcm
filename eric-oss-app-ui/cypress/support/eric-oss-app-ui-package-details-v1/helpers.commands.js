Cypress.Commands.add('visitPackageDetailsPageV1', () => {
    cy.visit('/#eric-oss-app-ui-package-details-v1?appid=1')
});

Cypress.Commands.add('getPackageDetailsContainerV1', () => {
    cy.get('eui-container').shadow()
    .find('e-eric-oss-app-ui-package-details-v1').shadow();
});

Cypress.Commands.add('getTableContainerV1', () => {
    cy.getPackageDetailsContainerV1()
      .find('[data-cy="artifacts-panel"]').find('[data-cy="artifacts-table__content"]')
      .find('[data-cy="artifacts-table"]').shadow().find('table')
});

Cypress.Commands.add('getEnableDisableSwitchV1', () => {
    cy.getPackageDetailsContainerV1()
      .find('eui-switch[data-cy="enable-disable__switch"]');
});

Cypress.Commands.add('getDeleteAppButtonV1', () => {
    cy.get('eui-container').shadow()
    .find('eui-app-bar').shadow()
    .find('#delete-app-btn').click();
});

Cypress.Commands.add('getDeleteDialogBoxV1', () => {
    cy.getPackageDetailsContainerV1()
    .find('e-delete-app-v1').shadow()
    .find('eui-dialog[data-cy="dialog-box"]');
});