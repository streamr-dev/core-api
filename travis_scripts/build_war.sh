#!/usr/bin/env bash
grails clean
npm install
npm run build
grails prod war
mkdir build
cp $(pwd)/target/ROOT.war $(pwd)/build