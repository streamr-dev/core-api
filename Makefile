version := $(shell git describe --tags --always --dirty="-dev")
date := $(shell date -u '+%Y-%m-%dT%H:%M:%SZ')
node_version := 10.16.3

.SHELLFLAGS := -c # Run commands in a -c flag
.ONESHELL: ; # recipes execute in same shell
#.SILENT: ; # no need for @
.NOTPARALLEL: ; # wait for this target to finish
.EXPORT_ALL_VARIABLES: ; # send all vars to shell
#.DEFAULT_GOAL := xxx

.PHONY: test-unit
test-unit:
	grails test-app -unit --stacktrace

.PHONY: test-integration
test-integration:
	grails test-app -integration --stacktrace

.PHONY: test-rest
test-rest:
	cd rest-e2e-tests && $(HOME)/.nvm/versions/node/v$(node_version)/bin/npm test

.PHONY: build-war
build-war:
	grails prod war

.PHONY: build-docker
build-docker: build-war
	docker build -t streamr-dev/engine-and-editor:dev .
