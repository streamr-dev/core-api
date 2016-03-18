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
grails dbm-gorm-diff $FILENAME --add $2

# Add package to beginning of file
{ printf "package core\n"; cat grails-app/migrations/$FILENAME; } > grails-app/migrations/$FILENAME.new
mv grails-app/migrations/$FILENAME{.new,}
