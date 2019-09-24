#!/bin/bash

REPORT="$(pwd)/target/test-reports/html/failed.html"
if [ -f "$REPORT" ]; then
	sed '/<script/,/<\/script>/d' "$REPORT" | sed 's/<[^>]*>//g' | sed '/^$/d'
fi
