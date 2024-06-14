#!/bin/sh
# Shell script loading card_transactions data to MySQL!
# Call command - ./sqoop_export_card_txns.sh quickstart.cloudera:3306 bigdataproject root card_transactions
DBSERVER=${1}
DBNAME=${2}
DBUSER=${3}
DBTABLE=${4}
sqoop export \
-Dhadoop.security.credential.provider.path=jceks://hdfs/user/cloudera/mysql.dbpassword.jceks \
--connect jdbc:mysql://${DBSERVER}/${DBNAME} \
--username ${DBUSER} \
--password-alias mysql.bigdataproject.password \
--table ${DBTABLE} \
--export-dir project_input_data/card_transactions.csv \
--fields-terminated-by ','