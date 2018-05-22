sudo /etc/init.d/mysql stop
npm install
git clone https://github.com/streamr-dev/streamr-docker-dev.git
$TRAVIS_BUILD_DIR/streamr-docker-dev/streamr-docker-dev/bin.sh start 1
grails clean
grails test-app -integration