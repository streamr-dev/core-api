OWNER=streamr
IMAGE_NAME=engine-and-editor
VCS_REF=`git rev-parse --short HEAD`
IMAGE_VERSION=0.2.$(TRAVIS_BUILD_NUMBER)
QNAME=$(OWNER)/$(IMAGE_NAME)

GIT_TAG=$(QNAME):$(VCS_REF)
BUILD_TAG=$(QNAME):$(IMAGE_VERSION)
LATEST_TAG=$(QNAME):latest


install-submodules:
	sed -i 's/git@github.com:/https:\/\/github.com\//' .gitmodules
	git submodule update --init --recursive

unit-test: install-submodules
	grails clean
	grails test-app -unit

install-driver:
	wget -N http://chromedriver.storage.googleapis.com/2.37/chromedriver_linux64.zip -P ~/
	unzip ~/chromedriver_linux64.zip -d ~/
	rm ~/chromedriver_linux64.zip
	sudo mv -f ~/chromedriver /usr/local/bin/chromedriver
	sudo chown root:root /usr/local/bin/chromedriver
	sudo chmod 0755 /usr/local/bin/chromedriver

integration-test: install-submodules
	sudo /etc/init.d/mysql stop
	npm install
	git clone https://github.com/streamr-dev/streamr-docker-dev.git
	$(TRAVIS_BUILD_DIR)/streamr-docker-dev/streamr-docker-dev/bin.sh start 1
	grails clean
	grails test-app -integration

functional-test: install-submodules install-driver
	sudo /etc/init.d/mysql stop
	npm install
	git clone https://github.com/streamr-dev/streamr-docker-dev.git /tmp/streamr-docker-dev
	/tmp/streamr-docker-dev/streamr-docker-dev/bin.sh start 1

functional-group-1:
	grails test-app -functional

functional-group-2:
	grails test test-app functional: ExampleSpec ExportCSVFunctionalSpec ForgotPasswordSpec InputModuleDashboardSpec InputModuleLiveSpec LiveSpec

build-war:
	grails clean
	npm install
	npm run build
	grails prod war
	mkdir build
	cp $(PWD)/target/ROOT.war $(PWD)/build

docker-build:
	docker build \
		--build-arg VCS_REF=$(VCS_REF) \
		--build-arg IMAGE_VERSION=$(IMAGE_VERSION) \
		-t $(GIT_TAG) .

docker-lint:
	docker run -it --rm -v "$(PWD)/Dockerfile:/Dockerfile:ro" redcoolbeans/dockerlint

docker-tag:
	docker tag $(GIT_TAG) $(BUILD_TAG)
	docker tag $(GIT_TAG) $(LATEST_TAG)

docker-login:
	@docker login -u "$(DOCKER_USER)" -p "$(DOCKER_PASS)"

docker-push: docker-login
	docker push $(LATEST_TAG)


