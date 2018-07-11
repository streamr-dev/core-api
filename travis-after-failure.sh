#!/bin/bash
sed '/<script/,/<\/script>/d' "$(pwd)/target/test-reports/html/failed.html" | sed 's/<[^>]*>//g' | sed '/^$/d'
