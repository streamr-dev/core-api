#!/bin/bash

sudo /etc/init.d/mysql stop
(cd rest-e2e-tests && npm install)
git clone https://github.com/streamr-dev/streamr-docker-dev.git

# same as streamr-docker-dev bind-ip
sudo ifconfig docker0 10.200.10.1/24

# Start everything except engine-and-editor
"$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh" start 1
# Print app output to console
"$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh" log -f &

# Allow time for services to start
echo "Sleeping for 30 seconds..." && sleep 30

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

# Wait for network to come up
while true; do
	http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/v1/volume)
	if [ "$http_code" -eq 200 ]; then
		echo "brokers up and running"
		break
	else
		echo "brokers not receiving connections"
		sleep 5s
	fi
done

(cd rest-e2e-tests && npm test)
