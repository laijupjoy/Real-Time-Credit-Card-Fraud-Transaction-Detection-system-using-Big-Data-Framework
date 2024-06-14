#!/bin/sh
# Shell script loading member_score data to Hive!
# ./sqoop_import_member_score.sh database-2.cl4c0rtglkdz.ap-south-1.rds.amazonaws.com BankingPrj admin member_score
DBSERVER=${1}
DBNAME=${2}
DBUSER=${3}
DBTABLE=${4}

sqoop import \
-Dhadoop.security.credential.provider.path=jceks://hdfs/user/cloudera/amazonrds.dbpassword.jceks \
--connect jdbc:mysql://${DBSERVER}/${DBNAME} \
--username ${DBUSER} \
--password-alias amazonrds.bigdataproject.password \
--table ${DBTABLE} \
--target-dir /project_input_data/member_score \
--delete-target-dir