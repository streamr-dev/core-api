#!/bin/bash

set -e -u -o pipefail

if [ $# -eq 0 ] ; then
	echo "error: give test name pattern as first argument"
	exit 1
fi
NAME=${1-}
find test -name "*${NAME}*Spec.groovy" | while read -r file; do
	if [ -e "$file" ]; then
		git rm "$file"
		git commit -m "Remove $file"
	else
		echo "WARNING: file $file does not exits."
	fi
done

