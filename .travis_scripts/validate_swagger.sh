#!/bin/bash

set -e

if ! curl --silent --fail --max-time 5 -o /dev/null http://online.swagger.io; then
       echo "Skipping Swagger validation since swagger.io is down" 1>&2
       exit 0
fi

result=$(./scripts/validate-swagger)
if [ "$result" != "{}" ]; then
	echo "Swagger validation error: $result"  1>&2
	exit 1
fi

