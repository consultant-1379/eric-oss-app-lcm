#!/bin/bash
npm start  &
echo "UI starting up"
sleep 30
npx cypress run --env allure=true,allureResultsPath='cypress-allure' --spec /eui_client/cypress/e2e