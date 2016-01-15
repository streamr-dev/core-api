#!/bin/bash

set -e

if ! [ -n "$1" ]
then
	echo "Usage: scripts/copy-test-db.sh <YOUR_SUFFIX>"
	exit 1
fi

if ! [ -n "$WORKSPACE" ]
then
	WORKSPACE="."
fi

source $WORKSPACE/scripts/parse-env.sh $1

echo Copying database $DBSOURCE to $DBNAME
$mysql -e 'DROP DATABASE IF EXISTS '$DBNAME';'
$mysql -e 'CREATE DATABASE '$DBNAME' CHARACTER SET utf8 COLLATE utf8_general_ci;'
$mysql -e "GRANT ALL ON ${DBNAME}.* TO 'unifina-test'@'%';"
$mysqldump --opt $DBSOURCE | $mysql $DBNAME
