#!/usr/bin/env bash
grails clean
npm install
npm run build
grails prod war
mkdir build
cp $(pwd)/target/ROOT.war $(pwd)/build
mv $(pwd)/.appspec.yml $(pwd)/build/appspec.yml
tar -czvf $(pwd)/build/ee.tar -C $(pwd)/build ROOT.war appspec.yml