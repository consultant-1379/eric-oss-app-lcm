Cypress.Commands.add('visitAdminHomePage', () => {
    cy.visit('/#eric-oss-app-ui');
});

Cypress.Commands.add('getAdminHomeContainer', () => {
    cy.get('eui-container').shadow()
    .find('e-eric-oss-app-ui').shadow();
});

Cypress.Commands.add('getApplicationLayerButton', () => {
    cy.get('eui-container').shadow()
    .find('eui-app-bar').shadow();
});


Cypress.Commands.add('getAdminContainer', () => {
    cy.get('eui-container').shadow()
    .find('e-eric-oss-app-ui').shadow();
});

Cypress.Commands.add('getOnboardingDialog', () => {
    cy.getAdminHomeContainer()
    .find('e-app-onboard').shadow()
    .find('eui-dialog');
});

Cypress.Commands.add('getOnboardingTableContainer', () => {
    cy.getAdminHomeContainer()
    .find('e-app-table').shadow()
    .find('eui-table').shadow();
});