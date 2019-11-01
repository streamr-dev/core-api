version := $(shell git describe --tags --always --dirty="-dev")
date := $(shell date -u '+%Y-%m-%dT%H:%M:%SZ')

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
	cd rest-e2e-tests && $(HOME)/.nvm/versions/node/v8.12.0/bin/npm test
