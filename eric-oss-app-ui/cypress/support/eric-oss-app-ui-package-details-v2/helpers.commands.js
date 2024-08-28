Cypress.Commands.add('visitPackageDetailsPageV2', () => {
    cy.visit('/#eric-oss-app-ui-package-details-v2?appid=7b8319dd-0198-4d4c-acf2-3fe352144951')
});

Cypress.Commands.add('getPackageDetailsContainerV2', () => {
    cy.get('eui-container').shadow()
    .find('e-eric-oss-app-ui-package-details-v2').shadow();
});

Cypress.Commands.add('getTableContainerV2', () => {
    cy.getPackageDetailsContainerV2()
      .find('[data-cy="artifacts-panel"]').find('[data-cy="artifacts-table__content"]')
      .find('[data-cy="artifacts-table"]').shadow().find('table')
});

Cypress.Commands.add('getEnableDisableSwitchV2', () => {
    cy.getPackageDetailsContainerV2()
      .find('eui-switch[data-cy="enable-disable__switch"]');
});

Cypress.Commands.add('getDeleteAppButtonV2', () => {
    cy.get('eui-container').shadow()
    .find('eui-app-bar').shadow()
    .find('#delete-app-btn').click();
});

Cypress.Commands.add('getDeleteDialogBoxV2', () => {
    cy.getPackageDetailsContainerV2()
    .find('eui-dialog[data-cy="dialog-box"]');
});