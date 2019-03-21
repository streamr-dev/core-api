#!/bin/bash

flow="./node_modules/.bin/flow"
if [ -x $flow ]; then
	$flow stop
else
	pkill -KILL flow
fi

files=(
target
node_modules
.slcache
"$HOME/.grails"
"$HOME/.m2"
)

for file in "${files[@]}"; do
	rm -rf "$file"
done
grails clean-all

