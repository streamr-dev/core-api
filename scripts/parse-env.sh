#!/bin/bash

DSCONFIG=$WORKSPACE/grails-app/conf/DataSource.groovy
BRANCHNAME=`echo $GIT_BRANCH | cut -d'/' -f2`
DBNAME=`echo $JOB_NAME'_'$BRANCHNAME | sed 's/-/_/g'`
DBSOURCE=core_test
MYSQL_PW=Trez2tuV

git='/usr/local/git/bin/git'
mysql='/usr/bin/mysql -u root --password='$MYSQL_PW
mysqldump='/usr/bin/mysqldump -u root --password='$MYSQL_PW
