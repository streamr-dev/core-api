#!/bin/bash
npm list -g less > /dev/null || (echo "Installing less" && npm install -g less)
npm list -g less-plugin-glob > /dev/null || (echo "Installing less-plugin-glob" && npm install -g less-plugin-glob)

lessc --glob web-app/less/main.less > web-app/css/compiled-less/main.css --silent && echo "$(date +"%Y-%m-%d %H:%M:%S") - Less files compiled successfully!"