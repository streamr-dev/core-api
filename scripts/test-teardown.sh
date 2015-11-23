#!/bin/bash

if ! [ -n "$GIT_BRANCH" ]
then
	echo "Error: GIT_BRANCH is not defined!"
	exit 1
fi

source $WORKSPACE/scripts/parse-env.sh `echo $GIT_BRANCH | cut -d'/' -f2`

# drop test db
$mysql -e 'DROP DATABASE IF EXISTS '$DBNAME';'

$git checkout $DSCONFIG
