#!/usr/bin/env bash
sudo /etc/init.d/mysql stop
(cd rest-e2e-tests && npm install)
git clone https://github.com/streamr-dev/streamr-docker-dev.git
$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh start mysql
echo "Sleeping for 30 seconds..." && sleep 30
( nohup grails test run-app --non-interactive 2>&1 > server-log.txt & ) # Got from: https://stackoverflow.com/questions/9779408/grails-2-0-cant-do-run-app
bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' http://localhost:8081/streamr-core/api/v1/products)" != "200" ]]; do sleep 5; done' # Thanks: https://gist.github.com/rgl/f90ff293d56dbb0a1e0f7e7e89a81f42
(cd rest-e2e-tests && npm test)