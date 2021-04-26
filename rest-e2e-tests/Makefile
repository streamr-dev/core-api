#
# https://makefiletutorial.com
# https://clarkgrubb.com/makefile-style-guide
#

# This Makefile uses .ONESHELL option supported by Make 3.82
ifeq ($(filter oneshell,$(.FEATURES)),)
$(error error: Your version of make ($(shell make -v|head -1|cut -f 3 -d ' ')) does not support .ONESHELL)
endif

LANG = en_US.UTF-8
SHELL = bash
.SHELLFLAGS = -eu -o pipefail -c # run '/bin/bash ... -c /bin/cmd'
.ONESHELL:
.DEFAULT_GOAL = all

nvm_brew = /usr/local/opt/nvm/nvm.sh
ifneq ("$(wildcard $(nvm_brew))", "")
	nvm_sh = $(nvm_brew)
endif
nvm_default = $(HOME)/.nvm/nvm.sh
ifneq ("$(wildcard $(nvm_default))", "")
	nvm_sh = $(nvm_default)
endif
node_version = $(shell cat .nvmrc)
define npm
	@$(eval npm_args=$(1))
	bash -e -o pipefail -l -c "source $(nvm_sh) && nvm exec $(node_version) npm $(npm_args)"
endef

all: clean vendor test/server/wait test ## Set up and test

node_modules: ## Run 'npm ci' if directory doesn't exist
	$(call npm, ci)

vendor: node_modules ## Install source code packages

core_api_status_check_url := http://localhost:8081/streamr-core/api/v1/users/me
.PHONY: test/server/wait
test/server/wait: ## Wait for server to come online
	export i=1
	while true; do
		http_code=$$(curl --silent --max-time 1 --output /dev/null --write-out "%{http_code}" $(core_api_status_check_url)) || true
		if [ "$$http_code" -eq 401 ]; then
			echo "test/server/wait: $$(date -u) core-api up and running"
			break
		else
			echo "test/server/wait: $$(date -u) core-api responding with HTTP status code $$http_code at $(core_api_status_check_url)"
			if [ "$$http_code" -eq 000 ]; then
				sleep 3
			fi
		fi
		i=$$(expr $$i + 1)
		if [ $$i -eq 70 ]; then
			echo "test/server/wait: timeout while waiting for core-api to come online" 1>&2
			exit 1
		fi
	done

.PHONY: test
test: vendor test/e2e test/stress ## Run tests

.PHONY: test/e2e
test/e2e: ## Run end to end tests
	$(call npm, run test:e2e)

.PHONY: test/stress
test/stress: ## Run stress tests
	$(call npm, run test:stress)

.PHONY: clean
clean: ## Remove generated files
	rm -rf \
		rest-e2e-tests/test-data/bigfile.txt \
		node_modules

.PHONY: help
help: ## Show Help
	@grep -E '^[a-zA-Z0-9_\-\/]+%?:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "%-20s %s\n", $$1, $$2}' | sort

