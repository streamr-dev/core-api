#!/bin/bash

set -e

streamr-docker-dev start mysql redis --wait

# Report generation phase will crash if a SecurityManager is installed. Fix: skip report generation with -no-reports
grails test-app -no-reports -integration --stacktrace --verbose
