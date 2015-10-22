#!/bin/bash

DSCONFIG=$WORKSPACE/grails-app/conf/DataSource.groovy
BRANCHNAME=`echo $GIT_BRANCH | cut -d'/' -f2`
DBNAME=`echo $JOB_NAME'_'$BRANCHNAME | cut -c1-64 | sed 's/-/_/g'` # max db name length is 64 chars
DBSOURCE=core_test
MYSQL_PW=Trez2tuV

git='/usr/local/git/bin/git'
mysql='/usr/bin/mysql -u root --password='$MYSQL_PW
mysqldump='/usr/bin/mysqldump -u root --password='$MYSQL_PW
