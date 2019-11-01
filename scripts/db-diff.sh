#!/bin/bash

if ! [ -n "$1" ]
then
	echo "Usage: ./scripts/db-diff.sh description-of-your-changes"
	exit 1
fi

DATE=`date +%Y-%m-%d`
FILENAME="core/$DATE-$1.groovy"
echo $FILENAME

# Run the migration diff script
grails dbm-gorm-diff $FILENAME --add $2 --stacktrace

# Add package to beginning of file
{ printf "package core\n"; cat grails-app/migrations/$FILENAME; } > grails-app/migrations/$FILENAME.new
mv grails-app/migrations/$FILENAME{.new,}

sed -i '' -e '/^$/d' grails-app/migrations/changelog.groovy # remove empty lines
sed -i '' -e '/^$/d' -e 's/[[:space:]](generated)//' "grails-app/migrations/$FILENAME" # replace " (generated)" with ""
# replace generated numeric (len 13-14) changeset id with $1
sed -i '' -E 's/^	changeSet\(author: "([a-zA-Z]+)", id: "[0-9]{13,14}-([0-9])"\) \{$/	changeSet\(author: "\1", id: "'"$1"'-\2"\) \{/g' "grails-app/migrations/$FILENAME"

