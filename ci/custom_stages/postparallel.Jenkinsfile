#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/custom_ruleset.yaml"
def mimer_ruleset = "ruleset2.0.mimer.yaml"

stage('Custom Generate Docs') {
    sh "${bob} -r ${ruleset} generate-docs:lint"
    sh "${bob} -r ${ruleset} generate-docs:md-to-pdf"
    sh "${bob} -r ${ruleset} generate-docs:md-to-html"
}


if(env.RELEASE){
    stage('Custom Sonarqube UI') {
        withSonarQubeEnv("${env.SQ_SERVER}") {
            sh "${bob} -r ${ruleset} sonar-enterprise-release:scan-ui"
        }
    }

    stage('Marketplace Upload') {
        withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS'),string(credentialsId: 'EAPPLCM_ADP_MARKETPLACE_TOKEN', variable: 'EAPPLCM_ADP_MARKETPLACE_TOKEN')]) {
            sh "${bob} -r ${ruleset} marketplace-upload-in-development"
            sh "${bob} -r ${ruleset} marketplace-upload-release"
            sh "${bob} -r ${ruleset} marketplace-upload-refresh"
        }
    }
}

if(!env.RELEASE) {
    stage ('FOSS Validation for Mimer') {
        if ( env.MUNIN_UPDATE_ENABLED == "true" ) {
            withCredentials([string(credentialsId: 'munin_token', variable: 'MUNIN_TOKEN')]) {
                sh "${bob} -r ${mimer_ruleset} munin-update-version"
            }
        }
    }
}


