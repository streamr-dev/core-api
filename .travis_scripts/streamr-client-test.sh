#!/bin/bash
set -e

sudo /etc/init.d/mysql stop
if [ ! -d streamr-docker-dev ]; then # Skip clone on subsequent attemps.
	git clone https://github.com/streamr-dev/streamr-docker-dev.git
fi
## Switch EE tag to the one built locally
sed -i "s/engine-and-editor:dev/engine-and-editor:local/g" $TRAVIS_BUILD_DIR/streamr-docker-dev/docker-compose.override.yml
sudo ifconfig docker0 10.200.10.1/24
"$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh" start smart-contracts-init nginx engine-and-editor --wait

## Setup testing Tool
git clone https://github.com/streamr-dev/streamr-client-testing.git
cd $TRAVIS_BUILD_DIR/streamr-client-testing
npm ci
gradle fatjar
java -jar build/libs/client_testing-1.0-SNAPSHOT.jar -m stream-cleartext-signed
