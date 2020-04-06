SHELL = /bin/bash
.SHELLFLAGS := -c # run 'bash -c /bin/cmd'
.ONESHELL: ; # recipes execute in same shell
#.SILENT: ; # no need for @
.NOTPARALLEL: ; # wait for this target to finish
.EXPORT_ALL_VARIABLES: ; # send all vars to shell
.DEFAULT_GOAL := test-unit

NVM_DIR=$(HOME)/.nvm

# Testing recipes

.PHONY: test-unit
test-unit:
	grails test-app -unit --stacktrace

.PHONY: test-integration
test-integration:
	grails test-app -integration --stacktrace

.PHONY: test-rest
test-rest:
	. /usr/local/opt/nvm/nvm.sh && nvm use && cd rest-e2e-tests && npm test

# Development recipes

.PHONY: build-war-dev
build-war-dev: clean
	grails test war

.PHONY: compile
compile:
	grails compile

.PHONY: dependency-report
dependency-report:
	grails dependency-report | tee dependencies.txt

.PHONY: run-app-test
run-app-test:
	grails test run-app

.PHONY: run-app-dev
run-app-dev:
	grails dev run-app

.PHONY: factory-reset
factory-reset:
	streamr-docker-dev factory-reset
	streamr-docker-dev start --except engine-and-editor

.PHONY: wipe
wipe:
	streamr-docker-dev wipe
	streamr-docker-dev start --except engine-and-editor

.PHONY: start
start:
	streamr-docker-dev start --except engine-and-editor

# Docker recipes

.PHONY: docker-build-dev
docker-build-dev: build-war-dev
	docker build -t streamr/engine-and-editor:dev .

.PHONY: docker-push-dev
docker-push-dev: docker-build-dev
	docker push streamr/engine-and-editor:dev

.PHONY: docker-run-dev
docker-run-dev:
	docker run -i -t -d --rm -p 8081:8081/tcp streamr/engine-and-editor:dev

.PHONY: docker-login
docker-login:
	docker login -u DOCKER_USER -p DOCKER_PASS

# Auxiliary recipes

.PHONY: clean
clean:
	rm -rf tomcat.8081/work
	rm -rf target
	rm -rf .slcache
	rm -rf "$$HOME/.grails"
	grails clean-all

.PHONY: help
help: ## Show Help
	@grep -E '^[a-zA-Z_-]+%?:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "%-15s %s\n", $$1, $$2}'
