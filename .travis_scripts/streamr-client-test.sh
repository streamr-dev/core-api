#!/bin/bash
set -e

streamr-docker-dev start --except hsl-demo --wait

## Setup testing Tool
git clone https://github.com/streamr-dev/streamr-client-testing.git
cd $TRAVIS_BUILD_DIR/streamr-client-testing
npm ci
./gradlew fatjar
java -jar build/libs/client_testing-1.0-SNAPSHOT.jar -s stream-cleartext-signed
