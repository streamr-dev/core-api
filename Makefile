
.SHELLFLAGS := -c # Run commands in a -c flag
.ONESHELL: ; # recipes execute in same shell
#.SILENT: ; # no need for @
.NOTPARALLEL: ; # wait for this target to finish
.EXPORT_ALL_VARIABLES: ; # send all vars to shell
.DEFAULT_GOAL := test-unit

NVM_DIR=$(HOME)/.nvm

# Testing targets

.PHONY: test-unit
test-unit:
	grails test-app -unit --stacktrace

.PHONY: test-integration
test-integration:
	grails test-app -integration --stacktrace

.PHONY: test-rest
test-rest:
	. /usr/local/opt/nvm/nvm.sh && nvm use && cd rest-e2e-tests && npm test

# Development targets

.PHONY: build-war-dev
build-war-dev: clean
	grails test war

.PHONY: docker-build-dev
docker-build-dev: build-war-dev
	docker build -t streamr/engine-and-editor:dev .

.PHONY: docker-push-dev
docker-push-dev: docker-build-dev
	docker push streamr/engine-and-editor:dev

.PHONY: docker-run-dev
docker-run-dev:
	docker run -i -t -d --rm -p 8081:8081/tcp streamr/engine-and-editor:dev

# Auxiliary targets

.PHONY: docker-login
docker-login:
	docker login -u DOCKER_USER -p DOCKER_PASS

.PHONY: clean
clean:
	rm -rf tomcat.8081/work
	rm -rf target
	rm -rf .slcache
	rm -rf "$$HOME/.grails"
	grails clean-all
	grails compile
