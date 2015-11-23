#!/bin/bash

if ! [ -n "$GIT_BRANCH" ]
then
	echo "Error: GIT_BRANCH is not defined!"
	exit 1
fi

BRANCH=`echo $GIT_BRANCH | cut -d'/' -f2`
source $WORKSPACE/scripts/parse-env.sh $BRANCH

# drop test db
$mysql -e 'DROP DATABASE IF EXISTS '$DBNAME';'

$git checkout $DSCONFIG
