#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/custom_ruleset.yaml"

stage('Custom package UI') {
    sh "${bob} -r ${ruleset} package-local:image-push-ui-internal"
    sh "${bob} -r ${ruleset} package-jars"
}

