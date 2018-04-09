OWNER=streamr
IMAGE_NAME=engine-and-editor
VCS_REF=`git rev-parse --short HEAD`
IMAGE_VERSION=0.2.$(TRAVIS_BUILD_NUMBER)
QNAME=$(OWNER)/$(IMAGE_NAME)

GIT_TAG=$(QNAME):$(VCS_REF)
BUILD_TAG=$(QNAME):$(IMAGE_VERSION)
LATEST_TAG=$(QNAME):latest

unit-test:
	grails clean
	grails test-app -unit

integration-test:
	sudo /etc/init.d/mysql stop
	npm install
	git clone https://github.com/streamr-dev/streamr-docker-dev.git
	$(TRAVIS_BUILD_DIR)/streamr-docker-dev/streamr-docker-dev/bin.sh start 1
	grails clean
	grails test-app -integration

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
