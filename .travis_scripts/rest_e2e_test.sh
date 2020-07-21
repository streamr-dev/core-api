#!/bin/bash

sudo /etc/init.d/mysql stop
echo "node version: " $(node --version)
(cd rest-e2e-tests && npm install)

git clone https://github.com/streamr-dev/streamr-docker-dev.git

# same as streamr-docker-dev bind-ip
sudo ifconfig docker0 10.200.10.1/24

# Start everything except engine-and-editor
"$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh" start --except engine-and-editor

# Print app output to console
"$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh" log -f &

# Wait for services to start
"$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh" wait

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
