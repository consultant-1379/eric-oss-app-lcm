# eric-oss-app-ui App Administrator

This is a skeleton implementation of eric-oss-app-ui App Administrator.
It can be used as a starting point to build the new App Admin UI for IDUN.

---

## Overview

This Micro Frontend service consists of the EUI SDK container & theme libraries
and basic server file used to host the project build.

---

## Public

The public folder contains 4 files which are listed below.

| name                | description                                                     |
|---------------------|-----------------------------------------------------------------|
| config.json         | configure the navigation menu based on ADP UI-Meta schema       |
| config.package.json | configure modules made available to the GUI Aggregator Service. |
| index.css           | style the Container                                             |
| index.html          | import and run the Theme and Container                          |

### Configuring the navigation menu

The JSON file, `config.package.json`, contains the specification for the
application. The schema for an app specification is from the [ADP spec for UI-Meta](https://euisdk.seli.wh.rnd.internal.ericsson.com/applications/gas-spec).

### Micro Frontend Applications

This Micro Frontend Service only contains an EUI SDK container which has been themed.
When the Micro Frontend Service is served (`npm run srv`),
GAS fetches its `config.package.json`, which contains information on each module
that this Micro Frontend Service want's to make available.

---

## Source

The src folder contains the code which developer implemented, it will be further
built upon by adding apps, components, panels etc. as needed for further development.

---

## Installation

`$ npm install`

## Testing

[`@open-wc/testing`](https://open-wc.org/docs/testing/testing-package/) is used
to unit test all components. It is an opinionated package that combines and
configures testing libraries to minimize the amount of ceremony required when
writing tests.

### Running tests

Run all tests against the Firefox headless browser.

``` shell
npm run test
```

Run all tests against all headless browsers (chrome, firefox and Safari).

``` shell
npm run test:all
```

Run all tests against Chrome headless browser in watch mode.

``` shell
npm run test:watch
```

## Cypress Integration

Cypress E2E testing has been included in this started application

### Run Cypress

To launch the cypress UI run following

### Development

- Open
npx cypress open --env configFile=developmentMocked
OR
npm run cypress:openDev

- Headless
npm run cypress:runDev
OR
npx cypress open --env configFile=developmentMocked

### Production

- Headless
npx cypress run --env configFile=productionMocked
OR
npm run cypress:runProd

- Open
npm run cypress:openProd
OR
npx cypress open --env configFile=productionMocked

---

## Development mode

To run the application in development mode, execute the following command.
A browser will open and display the apps.

> Import maps are used in this example and as yet are only supported by Chrome.

```shell
npm start
```

---

## Serve the Apps

It is possible to serve a built version of the applications.
You must first build the Micro Front Service...

> Import maps are used in this example and as yet are only supported by Chrome.

### build the Micro Front Service

```shell
npm run build
```

### serve the Micro Frontend Service

```shell
npm run srv
```
