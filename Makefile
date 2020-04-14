SHELL = /bin/bash
.SHELLFLAGS := -c # run 'bash -c /bin/cmd'
#.ONESHELL: ; # recipes execute in same shell
#.SILENT: ; # no need for @
.NOTPARALLEL: ; # wait for this target to finish
.EXPORT_ALL_VARIABLES: ; # send all vars to shell
.DEFAULT_GOAL := test-unit

NVM_DIR=$(HOME)/.nvm

# Testing recipes

.PHONY: test
test: test-unit test-integration test-rest ## Run unit, integration and REST API tests

.PHONY: test-unit
test-unit: ## Run unit tests
	grails test-app -unit --stacktrace

.PHONY: test-integration
test-integration: ## Run integration tests
	grails test-app -no-reports -integration --stacktrace --verbose

.PHONY: test-rest
test-rest: ## Run REST API tests
	. /usr/local/opt/nvm/nvm.sh && nvm use && cd rest-e2e-tests && npm test

# Development recipes

.PHONY: build-war-dev
build-war-dev: clean ## Build test war
	grails test war

.PHONY: compile
compile: ## Compile code
	grails compile

.PHONY: dependency-report
dependency-report: ## Generate Grails dependency report
	grails dependency-report | tee dependencies.txt

.PHONY: run-app-test
run-app-test: ## Run Grails test app
	grails test run-app

.PHONY: run-app-dev
run-app-dev: ## Run Grails dev app
	grails dev run-app

.PHONY: factory-reset
factory-reset: ## Run streamr-docker-dev factory-reset and start
	streamr-docker-dev factory-reset
	streamr-docker-dev start --except engine-and-editor

.PHONY: wipe
wipe: ## Run streamr-docker-dev wipe and start
	streamr-docker-dev wipe
	streamr-docker-dev start --except engine-and-editor

.PHONY: start
start: ## Run streamr-docker-dev start
	streamr-docker-dev start --except engine-and-editor

# Docker recipes

.PHONY: docker-build-dev
docker-build-dev: build-war-dev ## Build Docker dev container
	docker build -t streamr/engine-and-editor:dev .

.PHONY: docker-push-dev
docker-push-dev: docker-build-dev ## Push Docker dev container to registry
	docker push streamr/engine-and-editor:dev

.PHONY: docker-run-dev
docker-run-dev: ## Run Docker dev container locally
	docker run -i -t -d --rm -p 8081:8081/tcp streamr/engine-and-editor:dev

.PHONY: docker-login
docker-login: ## Login with Docker
	docker login -u DOCKER_USER -p DOCKER_PASS

# Auxiliary recipes

.PHONY: clean
clean: ## Clean generated files
	rm -rf tomcat.8081/work
	rm -rf target
	rm -rf .slcache
	rm -rf "$$HOME/.grails"
	grails clean-all

.PHONY: help
help: ## Show Help
	@grep -E '^[a-zA-Z_-]+%?:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "%-20s %s\n", $$1, $$2}'
