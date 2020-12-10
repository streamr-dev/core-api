#!/bin/bash

(cd rest-e2e-tests && npm ci)

# Start everything except engine-and-editor
streamr-docker-dev start mysql redis cassandra parity-node0 parity-sidechain-node0 bridge data-union-server broker-node-storage-1 nginx smtp

# Print app output to console
streamr-docker-dev log -f engine-and-editor &

# Wait for services to start
streamr-docker-dev wait

# Start engine-and-editor in the background
nohup grails test run-app --non-interactive &

# Wait for EE to come up
while true; do
	http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/v1/users/me)
	if [ "$http_code" -eq 401 ]; then
		echo "EE up and running"
		break
	else
		echo "EE not receiving connections"
		sleep 5s
	fi
done

(cd rest-e2e-tests && npm test)
