#!/bin/bash

set -e

source $WORKSPACE/scripts/parse-env.sh

$git reset --hard

echo Copying database $DBSOURCE to $DBNAME
$mysql -e 'DROP DATABASE IF EXISTS '$DBNAME';'
$mysql -e 'CREATE DATABASE '$DBNAME';'
time $mysqldump --opt $DBSOURCE | $mysql $DBNAME

sed -i -e 's/'$DBSOURCE'/'$DBNAME'/g' \
	-e 's/unifina-test/root/g' \
	-e 's/password.*/password="'$MYSQL_PW'"/' $DSCONFIG

