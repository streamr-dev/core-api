#!/bin/bash

compile () {
    lessc --glob web-app/less/main.less > web-app/css/compiled-less/main.css --silent && echo "$(date +"%Y-%m-%d %H:%M:%S") - Less files compiled successfully!"
}
compile || ( echo "
    Compiling less failed. Have you installed 'less' and 'less-plugin-glob' from npm?
" )