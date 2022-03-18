SHELL := /bin/bash
.SHELLFLAGS = --norc --noprofile -e -u -o pipefail -c
.DEFAULT_GOAL = build

.PHONY: build
build:
	mvn web3j:generate-sources

.PHONY: deploy
deploy:
	cp src/generated/java/com/streamr/network/contract/StreamRegistry.java ../src/java/com/streamr/network/contract/StreamRegistry.java

.PHONY:
clean:
	$(RM) -r \
		src/generated
