#!/bin/bash

set -e

if ! [ -n "$GIT_BRANCH" ]
then
	echo "Error: GIT_BRANCH is not defined!"
	exit 1
fi

source $WORKSPACE/scripts/parse-env.sh `echo $GIT_BRANCH | cut -d'/' -f2`

$git reset --hard

time $WORKSPACE/scripts/copy-test-db.sh $GIT_BRANCH
sed -i -e 's/'$DBSOURCE'/'$DBNAME'/g' $DSCONFIG
