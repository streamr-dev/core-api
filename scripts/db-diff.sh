#!/bin/bash

if [ -z "$1" ]; then
	echo "Usage: ./scripts/db-diff.sh description-of-your-changes"
	exit 1
fi

FILENAME="$(date +%Y-%m-%d)-$1.groovy"
FILEPATH="grails-app/migrations/core/$FILENAME"

# Run the migration diff script
if ! grails dbm-gorm-diff "core/$FILENAME" --add --stacktrace; then
	exit 1
fi

echo "package core" > "$FILEPATH.new"
cat "$FILEPATH" >> "$FILEPATH.new"
mv "$FILEPATH.new" "$FILEPATH"

sed -i '' -e '/^$/d' grails-app/migrations/changelog.groovy # remove empty lines
sed -i '' -e '/^$/d' -e 's/[[:space:]](generated)//' "$FILEPATH" # replace " (generated)" with ""
# replace generated numeric (len 13-14) changeset id with $1
sed -i '' -E 's/^	changeSet\(author: "([a-zA-Z]+)", id: "[0-9]{13,14}-([0-9])"\) \{$/	changeSet\(author: "\1", id: "'"$1"'-\2"\) \{/g' "$FILEPATH"

