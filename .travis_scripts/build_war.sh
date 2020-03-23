#!/bin/bash

set -e

grails prod war
mkdir build
cp "$(pwd)/target/ROOT.war" "$(pwd)/build"
# Set .appspec in root
mv "$(pwd)/.codedeploy/.appspec.yml" "$(pwd)/build/appspec.yml"
# Copy bash scripts to be deployed in the tar
mv "$(pwd)/.codedeploy" "$(pwd)/build/"
tar -czf "$(pwd)/build/ee-${TRAVIS_JOB_ID}.tar" -C "$(pwd)/build" ROOT.war appspec.yml .codedeploy
