LANG = en_US.UTF-8
SHELL = /bin/bash
.SHELLFLAGS := -c # run 'bash -c /bin/cmd'
.DEFAULT_GOAL := all

#.ONESHELL: recipe-name # recipes execute in same shell
#.SILENT: recipe-name # no need for @
#.NOTPARALLEL: recipe-name # wait for this target to finish
#.EXPORT_ALL_VARIABLES: recipe-name # send all vars to shell

grails := grails -plain-output

# Testing recipes

.PHONY: all
all: start-wait test-engine-start test-rest test-engine-stop
#all: clean test-unit test-integration start-wait test-engine-start test-rest test-engine-stop

.PHONY: test
test: test-unit test-integration test-rest ## Run unit, integration and REST API tests

test_report_html := $(shell pwd)/target/test-reports/html/failed.html
.PHONY: test-unit
test-unit: ## Run unit tests
	$(grails) test-app -unit -echoOut -echoErr || (exit_code=$$?; open $(test_report_html); exit $$exit_code)

.PHONY: test-integration
test-integration: ## Run integration tests
	$(grails) test-app -integration -no-reports --stacktrace --verbose

engine_status_check_url := http://localhost:8081/streamr-core/api/v1/users/me
.ONESHELL: test-rest
.PHONY: test-rest
test-rest: ## Run REST API tests
	export i=1
	#export min_timeout=0
	while true; do
		http_code=$$(curl -s -m 1 -o /dev/null -w "%{http_code}" $(engine_status_check_url))
		if [ "$$http_code" -eq 401 ]; then
			echo "test-rest: $$(date -u) engine up and running"
			break
		else
			echo "test-rest: $$(date -u) engine responding with HTTP status code $$http_code at $(engine_status_check_url)"
			if [ "$$http_code" -eq 000 ]; then
				sleep_timeout=$$(echo "(60 / ($$i / l(5))) / 2" | bc -l)
				sleep_timeout_int=$$(echo "($$sleep_timeout + 0.5) / 1" | bc)
				if [ "$$sleep_timeout_int" -le 5 ]; then
				#if [ $$min_timeout -eq 1 ] || [ "$$sleep_timeout_int" -le 5 ]; then
					#min_timeout=1
					sleep_timeout_int=5
				fi
				sleep $$sleep_timeout_int
			fi
		fi
		i=$$(expr $$i + 1)
		if [ $$i -eq 50 ]; then
			echo "test-rest: timeout while waiting for engine to come online"
			exit 1
		fi
	done
	cd rest-e2e-tests && . /usr/local/opt/nvm/nvm.sh && nvm use && npm test

rest_srv_test_log := rest-srv-test.log
rest_srv_test_pid := rest-srv-test.pid
rest_srv_tail_pid := rest-srv-tail.pid

.PHONY: test-engine-start
test-engine-start: ## Run Grails test app
	export AWS_PROFILE=automation-user-streamr-dev
	nohup grails test run-app --non-interactive > $(rest_srv_test_log) &
	echo $$! > $(rest_srv_test_pid)
	tail -f $(rest_srv_test_log) &
	echo $$! > $(rest_srv_tail_pid)

.PHONY: test-engine-stop
test-engine-stop: ## Kill processes started by test-engine-start
	kill $(shell cat $(rest_srv_test_pid))
	rm -rf $(rest_srv_test_pid)
	kill $(shell cat $(rest_srv_tail_pid))
	rm -rf $(rest_srv_tail_pid)

# Development recipes

.PHONY: compile
compile: ## Compile code
	$(grails) compile

.PHONY: dependency-report
dependency-report: ## Generate Grails dependency report to stdout and dependencies.txt
	$(grails) dependency-report | tee dependencies.txt

.PHONY: factory-reset
factory-reset: ## Run streamr-docker-dev factory-reset
	streamr-docker-dev factory-reset

.PHONY: wipe
wipe: ## Run streamr-docker-dev stop and wipe
	streamr-docker-dev wipe

.PHONY: start
start: ## Run streamr-docker-dev start
	streamr-docker-dev start --except engine-and-editor

.NOTPARALLEL: start-wait
.PHONY: start-wait
start-wait: ## Run streamr-docker-dev start --wait
	streamr-docker-dev start --wait --except engine-and-editor

.PHONY: stop
stop: ## Run streamr-docker-dev stop
	streamr-docker-dev stop

.SILENT: db-diff
.ONESHELL: db-diff
.PHONY: db-diff
db-diff: ## Run Grails 'grails dbm-gorm-diff' with extras. WARNING! This command is destructive.
	echo "please enter a description of the change (no spaces, separated by - char, for example: mv-secuser-to-profile)"; \
	read -e description; \
	if [ -z "$$description" ]; then \
		echo "db-diff: description is required" 1>&2; \
		exit 1; \
	fi; \
	export migration_file="$$(date +%Y-%m-%d)-$$description.groovy"; \
	export migration_path="grails-app/migrations/core/$$migration_file"; \
	export changelog_path="grails-app/migrations/changelog.groovy"; \
	grails dbm-gorm-diff "core/$$migration_file" --add --stacktrace; \
	echo "package core" > "$$migration_path.new"; \
	cat "$$migration_path" >> "$$migration_path.new"; \
	mv "$$migration_path.new" "$$migration_path"; \
	sed -i '' -e '/^$$/d' "$$changelog_path" ; \
	sed -i '' \
		-e '/^$$/d' \
		-e 's/[[:space:]](generated)//' \
		"$$migration_path"; \
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
clean: ## Remove all files created by this Makefile
	rm -rf tomcat.8081/work
	rm -rf target
	rm -rf .slcache
	rm -rf "$$HOME/.grails"
	rm -rf $(rest_srv_test_log)
	$(grails) clean-all

.PHONY: help
help: ## Show Help
	@grep -E '^[a-zA-Z_-]+%?:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "%-20s %s\n", $$1, $$2}'
