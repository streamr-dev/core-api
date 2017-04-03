#!/bin/bash

npm list -g watch > /dev/null || (echo "Installing watch" && npm install -g watch)
watch "scripts/compile-less.sh" web-app/less/
