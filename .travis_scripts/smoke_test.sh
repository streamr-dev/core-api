#!/bin/bash

set -e

sudo ifconfig docker0 10.200.10.1/24
git clone https://github.com/streamr-dev/streamr-docker-dev.git
sed -i -e "s#${OWNER}/${IMAGE_NAME}:dev#${OWNER}/${IMAGE_NAME}:local#g" "$TRAVIS_BUILD_DIR/streamr-docker-dev/docker-compose.override.yml"
"$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh" start 5

wait_time=10
for ((i = 0; i < 5; i++)); do
	if curl -sS "http://localhost:8081/streamr-core/api/v1/products"; then
		exit 0
	else
		echo "smoke_test.sh: attempting to connect to ee"
		echo "smoke_test.sh: retrying in $wait_time seconds"
		sleep $wait_time
		wait_time=$((2 * wait_time))
	fi
done
echo "smoke_test.sh: error in smoke test" 1>&2
exit 1
