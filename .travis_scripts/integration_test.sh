#!/bin/bash

set -e

sudo /etc/init.d/mysql stop
if [ ! -d streamr-docker-dev ]; then # Skip clone on subsequent attemps.
	git clone https://github.com/streamr-dev/streamr-docker-dev.git
fi
sudo ifconfig docker0 10.200.10.1/24
"$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh" start --except engine-and-editor --wait

# Report generation phase will crash if a SecurityManager is installed. Fix: skip report generation with -no-reports
grails test-app -no-reports -integration --stacktrace --verbose
