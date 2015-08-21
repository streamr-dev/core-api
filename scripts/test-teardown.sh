#!/bin/bash

source $WORKSPACE/bin/parse-env.sh

# drop test db
$mysql -e 'DROP DATABASE IF EXISTS '$DBNAME';'

$git checkout $DSCONFIG
$git checkout $BUILDCONFIG

