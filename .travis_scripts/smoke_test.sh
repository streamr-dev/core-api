#!/bin/bash

set -e

streamr-docker-dev start --wait

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
