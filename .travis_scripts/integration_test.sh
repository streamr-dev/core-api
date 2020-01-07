#!/bin/bash

set -e

sudo /etc/init.d/mysql stop
git clone https://github.com/streamr-dev/streamr-docker-dev.git
sudo ifconfig docker0 10.200.10.1/24
"$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh" start 1

# Report generation phase will crash if a SecurityManager is installed. Fix: skip report generation with -no-reports
grails test-app -no-reports -integration --stacktrace --verbose
