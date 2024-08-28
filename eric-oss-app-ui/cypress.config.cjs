const { defineConfig } = require("cypress");
const fs = require('fs-extra')
const path = require('path')
const allureWriter = require('@shelex/cypress-allure-plugin/writer');

function getConfigurationFile(file) { // eslint-disable-line
  const pathToConfigurationFile = path.resolve('cypress', 'config', `${file}.json`)
  if (!fs.existsSync(pathToConfigurationFile)) {
    return {
      env: {
        "apiUrl": "https://localhost:8082",
      },
    };
  }

  return fs.readJson(pathToConfigurationFile)
}

module.exports = defineConfig({
  projectId: 'kivdz2',
  video: true,

  e2e: {
    experimentalRunAllSpecs: true,
    setupNodeEvents(on, config) {
      allureWriter(on, config);
      return config;
    },
    env: {
      allureReuseAfterSpec: true,
    },
    baseUrl: 'http://localhost:8000',
    gasURL: 'https://gas.hart071-x5.ews.gic.ericsson.se',
    specPattern: 'cypress/e2e/**/*.{js,jsx,ts,tsx}',
  },
  retries: {
    "runMode": 2,
    "openMode": 2,
  },
});