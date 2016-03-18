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

DBHOST="dev.streamr"
MYSQL_PW=Trez2tuV
DBSOURCE=core_test
DBNAME=`echo $DBSOURCE'_'$1 | cut -c1-64 | sed 's/[-.]/_/g'` # max db name length is 64 chars
DBUSER="unifina-test"

mysql=`which mysql`
mysql="$mysql --host=$DBHOST -u root --password=$MYSQL_PW"
mysqldump=`which mysqldump`
mysqldump="$mysqldump --host=$DBHOST -u root --password=$MYSQL_PW"

echo "Database host: $DBHOST"
echo "Source database: $DBSOURCE"
echo "Target database: $DBNAME"

echo Copying database $DBSOURCE to $DBNAME
$mysql -e 'DROP DATABASE IF EXISTS '$DBNAME';'
$mysql -e 'CREATE DATABASE '$DBNAME' CHARACTER SET utf8 COLLATE utf8_general_ci;'
$mysql -e "GRANT ALL ON ${DBNAME}.* TO '${DBUSER}'@'%';"
$mysqldump --opt $DBSOURCE | $mysql $DBNAME
