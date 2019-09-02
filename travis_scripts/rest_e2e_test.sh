#!/usr/bin/env bash
sudo /etc/init.d/mysql stop
(cd rest-e2e-tests && npm install)
git clone https://github.com/streamr-dev/streamr-docker-dev.git

# Start everything except engine-and-editor
$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh start 1

echo "Sleeping for 30 seconds..." && sleep 30

# Start engine-and-editor in the background. To see the logs, remove the "> ee-logs.txt" redirect. TODO: add back the redirect
nohup grails test run-app --non-interactive &

# Wait for EE to come up
while true; do http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/streamr-core/api/v1/users/me); if [ $http_code = 401 ]; then echo "EE up and running"; break; else echo "EE not receiving connections"; sleep 5s; fi; done

# TODO: remove, show data-api logs
$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh restart data-api && $TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh log -f data-api &

# Wait for data-api to come up
while true; do http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8890/); if [ $http_code = 404 ]; then echo "Data-api up and running"; break; else echo "Data API not receiving connections"; sleep 5s; fi; done

(cd rest-e2e-tests && npm test)
