#!/bin/bash

set -e

if ! [ -n "$GIT_BRANCH" ]
then
	echo "Error: GIT_BRANCH is not defined!"
	exit 1
fi

BRANCH=`echo $GIT_BRANCH | cut -d'/' -f2`
source $WORKSPACE/scripts/parse-env.sh $BRANCH

#$git reset --hard

time $WORKSPACE/scripts/copy-test-db.sh $BRANCH
sed -i -e 's/'$DBSOURCE'/'$DBNAME'/g' $DSCONFIG
