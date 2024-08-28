#!/usr/bin/env groovy

def bob = "./bob/bob"
def ruleset = "ci/custom_ruleset.yaml"

stage('Custom Image UI') {
    sh "${bob} -r ${ruleset} image:docker-build-ui"
}

