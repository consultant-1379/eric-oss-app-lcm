
# App Lcm - App Manager Test Report

## Abstract

This document is intended as a way of showing the results of the GAT Test Cases
after being executed.

## General

### Introduction

The purpose of the Acceptance Report document is to provide support to Ericsson
Engineers that carry out the Installation & Acceptance test activity for
Application Manager.
Application Manager allows onboard apps (not applications), and then it
manages its life cycle, allowing the instantiation, monitoring, and termination
of Apps.
The document includes generic tests to allow customer acceptance of the product.

### Revision History

| Revision | Date        | Reason for Revision                      | Reviewer |
|----------|-------------|------------------------------------------|----------|
| A        | 29 Nov 2021 | First Revision of document               | EANLEAL  |
| B        | 20 Jan 2022 | Approved for Platform Services 1.2203    | EEIKMUY  |
| C        | 10 Feb 2022 | Maturity Level increase and fixes        | EANLEAL  |
| D        | 05 Apr 2022 | App Manager: Enabling and disabling apps | EZMURKI  |
| E        | 29 Jun 2022 | App Lcm: Delete app Instances            | ESHAKEA  |
| E        | 30 Jun 2022 | App Manager: Delete Apps                 | ESHAKEA  |
| E        | 04 Apr 2023 | Revision of document                     | ZRLACAR  |

### Application

This document describes how to test the features of Platform Services Version 2021 Q1

| Feature             | No. Feature | Title (node)        | Test no. | Impact on Network | Release Introduction |
|---------------------|-------------|---------------------|----------|-------------------|----------------------|
| Application Manager |             | Application Manager |          | No                |                      |

### Glossary

| Terms | Description                                                                                   |
|-------|-----------------------------------------------------------------------------------------------|
| App   | Automation Apps, small enough that reuses as much functionality as possible from the Platform |
| AM    | Application Manager                                                                           |

## Additional Information

NA

## Test Execution Time

| Activity / Test No. | Test Case                                       | Estimated Time | Comments                                          | Preparation                                                |
|---------------------|-------------------------------------------------|----------------|---------------------------------------------------|------------------------------------------------------------|
| GAT-AM-01           | Onboard one App                                 | 00:03:00       | Lcm                                               | URL, Previously logged in and .csar file available locally |
| GAT-AM-02           | Check the status of an Onboarded App            | 00:01:00       | Check status of Apps Onboarded                    | URL, Previously logged in and .csar file available locally |
| GAT-AM-03           | Enable an App for instantiation                 | 00:02:00       | Enable an App                                     | URL, Previously logged in                                  |
| GAT-AM-04           | Instantiate an App                              | 00:03:00       | Instantiation and checking status of App Instance | URL, Previously logged in and .csar file available locally |
| GAT-AM-05           | Monitor and check App Instance status           | 00:02:00       | Check Status of Apps Instance                     | URL, Previously logged in and .csar file available locally |
| GAT-AM-06           | Terminate an instance of an App                 | 00:03:00       | Terminate and Check Status of App Instance        | URL, Previously logged in and .csar file available locally |
| GAT-AM-07           | Disable an App to prevent further App instances | 00:02:00       | Disable an App                                    | URL, Previously logged in                                  |
| GAT-AM-08           | Delete an App Instance                          | 00:00:00       | Delete Instance and check if not exist            | URL, Previously logged in                                  |
| Total               |                                                 | 00:16:00       |                                                   |                                                            |

## Final Test Report

| Activity / Test No. | Test Case                                       | Result                                                                     | Comments                                          | Preparation                                                |
|---------------------|-------------------------------------------------|----------------------------------------------------------------------------|---------------------------------------------------|------------------------------------------------------------|
| GAT-AM-01           | Onboard one App                                 | ![#c5f015](https://via.placeholder.com/15/c5f015/000000?text=+) `Pass`     | Onboarding                                        | URL, Previously logged in and .csar file available locally |
| GAT-AM-02           | Check the status of an Onboarded App            | ![#c5f015](https://via.placeholder.com/15/c5f015/000000?text=+) `Pass`     | Check status of Apps Onboarded                    | URL, Previously logged in                                  |
| GAT-AM-03           | Enable an App for instantiation                 | ![#c5f015](https://via.placeholder.com/15/c5f015/000000?text=+) `Pass`     | Enable an App                                     | URL, Previously logged in                                  |
| GAT-AM-04           | Instantiate an App                              | ![#c5f015](https://via.placeholder.com/15/c5f015/000000?text=+) `Pass`     | Instantiation and checking status of App Instance | URL, Previously logged in                                  |
| GAT-AM-05           | Monitor and check App Instance status           | ![#c5f015](https://via.placeholder.com/15/c5f015/000000?text=+) `Pass`     | Check Status of Apps Instance                     | URL, Previously logged in                                  |
| GAT-AM-06           | Terminate an instance of an App                 | ![#c5f015](https://via.placeholder.com/15/c5f015/000000?text=+) `Pass`     | Terminate and Check Status of App Instance        | URL, Previously logged in                                  |
| GAT-AM-07           | Disable an App to prevent further App instances | ![#c5f015](https://via.placeholder.com/15/c5f015/000000?text=+) `Pass`     | Disable an App                                    | URL, Previously logged in                                  |
| GAT-AM-08           | Delete an App Instance                          |                                                                            | Delete Instance and check if not exist            | URL, Previously logged in                                  |
| Total 7             |                                                 | ![#c5f015](https://via.placeholder.com/15/c5f015/000000?text=+) `Pass 7/7` |                                                   |                                                            |
