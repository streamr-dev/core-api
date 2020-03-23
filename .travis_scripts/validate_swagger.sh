#!/bin/bash

set -e

result=$(./scripts/validate-swagger)
if [ "$result" != "{}" ]; then
	echo "Swagger validation error: $result"  1>&2
	exit 1
fi

