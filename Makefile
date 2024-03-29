#
# https://makefiletutorial.com
# https://clarkgrubb.com/makefile-style-guide
#

# This Makefile uses .ONESHELL option supported by Make 3.82
ifeq ($(filter oneshell,$(.FEATURES)),)
$(error error: Your version of make ($(shell make -v|head -1|cut -f 3 -d ' ')) does not support .ONESHELL)
endif

LANG = en_US.UTF-8
SHELL = /bin/bash
.SHELLFLAGS = -eu -o pipefail -c # run '/bin/bash ... -c /bin/cmd'
.ONESHELL:
.DELETE_ON_ERROR:
.DEFAULT_GOAL = test

grails = grails -plain-output

# Testing recipes

.PHONY: all
all: clean test-unit test-integration start-wait test-e2e ethereum-watcher network-explorer

.PHONY: test
test: test-unit test-integration test-e2e ## Run unit, integration and end to end tests

test_report_html = $$(pwd)/target/test-reports/html/failed.html
.PHONY: test-unit
test-unit: ## Run unit tests
	$(grails) test-app -unit -echoOut -echoErr || (exit_code=$$?; open $(test_report_html); exit $$exit_code)

.PHONY: test-integration
test-integration: ## Run integration tests
	$(grails) test-app -integration -no-reports --stacktrace --verbose

.PHONY: test-rest
test-rest:
	$(MAKE) -C rest-e2e-tests test

.PHONY: test-e2e
test-e2e:
	$(MAKE) -C rest-e2e-tests test/e2e

# Development recipes

.PHONY: idea
idea: ## Generate IntelliJ IDEA project files
	$(grails) --refresh-dependencies idea-print-project-settings

.PHONY: compile
compile: ## Compile code
	$(grails) compile

.PHONY: dependency-report
dependency-report: ## Generate Grails dependency report to stdout and dependencies.txt
	$(grails) dependency-report | tee dependencies.txt

.PHONY: run
run:
	$(grails) run-app

.PHONY: run-test
run-test:
	$(grails) test run-app

.PHONY: factory-reset
factory-reset: ## Run streamr-docker-dev factory-reset
	streamr-docker-dev factory-reset

.PHONY: wipe
wipe: ## Run streamr-docker-dev stop and wipe
	streamr-docker-dev wipe

services := mysql redis cassandra parity-node0 parity-sidechain-node0 bridge broker-node-no-storage-1 broker-node-storage-1 nginx smtp platform
.PHONY: start
start: ## Run streamr-docker-dev start ...
	streamr-docker-dev start $(services)
	streamr-docker-dev stop core-api

.PHONY: start-wait
start-wait: ## Run streamr-docker-dev start ... --wait
	streamr-docker-dev start $(services) --wait
	streamr-docker-dev stop core-api

.PHONY: stop
stop: ## Run streamr-docker-dev stop
	streamr-docker-dev stop

.PHONY: pull
pull: ## Run streamr-docker-dev pull
	streamr-docker-dev pull

.PHONY: ps
ps: ## Run streamr-docker-dev ps
	streamr-docker-dev ps

.PHONY: update
update: ## Run streamr-docker-dev update
	streamr-docker-dev update

shell-%: ## Run docker shell. Example: 'make shell-redis'
	streamr-docker-dev shell $*

.SILENT: db-diff
.PHONY: db-diff
db-diff: ## Run Grails 'grails dbm-gorm-diff' with extras. WARNING! This command is destructive.
	echo "please enter a description of the change (no spaces, separated by - char, for example: mv-secuser-to-profile)"
	read -e description
	if [ -z "$$description" ]; then \
		echo "db-diff: description is required" 1>&2
		exit 1
	fi
	mkdir -p grails-app/migrations/core
	export migration_file="$$(date +%Y-%m-%d)-$$description.groovy"
	export migration_path="grails-app/migrations/core/$$migration_file"
	export changelog_path="grails-app/migrations/changelog.groovy"
	grails dbm-gorm-diff "core/$$migration_file" --add --stacktrace
	echo "package core" > "$$migration_path.new"
	cat "$$migration_path" >> "$$migration_path.new"
	mv "$$migration_path.new" "$$migration_path"
	sed -i '' -e '/^$$/d' "$$changelog_path"
	sed -i '' \
		-e '/^$$/d' \
		-e 's/[[:space:]](generated)//' \
		"$$migration_path"
	sed -i '' \
		-E 's/^	changeSet\(author: "([a-zA-Z]+)", id: "[0-9]{13,14}-([0-9])"\) \{$$/	changeSet\(author: "\1", id: "'"$$description"'-\2"\) \{/g' \
		"$$migration_path"
# comments for sed scripts above per regex
# 1. remove empty lines
# 2. replace " (generated)" with ""
# 3. replace Grails generated numeric (len 13-14) changeset id with $DESCRIPTION

# db-diff TODO:
# Consider adding:
#	git add "$$migration_path" "$$changelog_path"
# ...to the end of the recipe, but how would db-diff-revert work then?

.PHONY: db-diff-revert
db-diff-revert: ## Remove files generated by db-diff. WARNING! This command is destructive.
	git clean -f grails-app/migrations/core
	git checkout grails-app/migrations/changelog.groovy

.PHONY: db-update
db-update:
	grails test dbm-update

.PHONY: db-rollback-1
db-rollback-1:
	grails dbm-rollback-count 1

# Docker recipes

.PHONY: docker-build-dev
docker-build-dev: ## Build Docker dev container
	docker build \
		--no-cache \
		--progress=plain \
		--build-arg GRAILS_WAR_ENV=test \
		--tag streamr/core-api:dev .

.PHONY: docker-push-dev
docker-push-dev: docker-build-dev ## Push Docker dev container to registry
	docker push docker.io/streamr/core-api:dev

.PHONY: docker-run-dev
docker-run-dev: ## Run Docker dev container locally
	docker run -i -t -d --rm -p 8081:8081/tcp streamr/core-api:dev

# Auxiliary recipes

.PHONY: clean
clean: ## Remove all files created by this Makefile
	$(RM) -r \
		dependencies.txt \
		tomcat.8081/work \
		target \
		.slcache \
		"$$HOME/.grails"
	$(grails) clean-all
	mkdir -p "$$HOME/.grails/scripts"

.PHONY: clean-all
clean-all: clean
	$(MAKE) -C rest-e2e-tests clean

.PHONY: help
help: ## Show Help
	@grep -E '^[a-zA-Z_-]+%?:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "%-20s %s\n", $$1, $$2}'|sort
