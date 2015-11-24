#!/bin/bash

if [ -n "$1" ]
then
	BRANCHNAME="$1"
else
	echo "Usage: parse-env.sh <BRANCHNAME>"
	exit 1
fi

DSCONFIG=$WORKSPACE/grails-app/conf/DataSource.groovy
DBHOST="dev.unifina"
MYSQL_PW=Trez2tuV
DBSOURCE=core_test
DBNAME=`echo $DBSOURCE'_'$BRANCHNAME | cut -c1-64 | sed 's/[-.]/_/g'` # max db name length is 64 chars

git=`which git`
mysql=`which mysql`
mysql="$mysql --host=$DBHOST -u root --password=$MYSQL_PW"
mysqldump=`which mysqldump`
mysqldump="$mysqldump --host=$DBHOST -u root --password=$MYSQL_PW"

echo "Database host: $DBHOST"
echo "Source database: $DBSOURCE"
echo "Target database: $DBNAME"
