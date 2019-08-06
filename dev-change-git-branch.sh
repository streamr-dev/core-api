#!/bin/bash

flow="./node_modules/.bin/flow"
if [ -x $flow ]; then
	$flow stop
else
	pkill -KILL flow
fi

files=(
target
.slcache
"$HOME/.grails"
)

for file in "${files[@]}"; do
	rm -rf "$file"
done
grails clean-all
grails compile

