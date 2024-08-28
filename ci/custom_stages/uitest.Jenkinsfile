#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/custom_ruleset.yaml"

try {
    stage('Custom Build UI') {
        sh "${bob} -r ${ruleset} build:docker-build-ui-test-image"
    }
} catch (e) {
    throw e
} finally {
    archiveArtifacts allowEmptyArchive: true, artifacts: '**/design-rule-check-report.*, **/target/site/checkstyle.html, **/target/site/pmd.html, **/target/site/cpd.html'
}

try {
    stage('Custom Permissions') {
        sh "chmod 777 'eric-oss-app-ui/cypress-allure/'"
        sh "chmod 777 'eric-oss-app-ui/coverage/'"
    }
} catch (e) {
    throw e
} finally {
    archiveArtifacts allowEmptyArchive: true, artifacts: '**/design-rule-check-report.*, **/target/site/checkstyle.html, **/target/site/pmd.html, **/target/site/cpd.html'
}

try {
    stage('Custom Tests') {
        sh "${bob} -r ${ruleset} test"
    }
} catch (e) {
    throw e
} finally {
    archiveArtifacts allowEmptyArchive: true, artifacts: '**/design-rule-check-report.*, **/target/site/checkstyle.html, **/target/site/pmd.html, **/target/site/cpd.html'
}

try {
    stage('Custom Cypress') {
        sh "${bob} -r ${ruleset} cypress-tests"
    }
} catch (e) {
    throw e
} finally {
    archiveArtifacts allowEmptyArchive: true, artifacts: '**/design-rule-check-report.*, **/target/site/checkstyle.html, **/target/site/pmd.html, **/target/site/cpd.html'
}

