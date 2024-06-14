#!/bin/sh
# Shell script loading member_details data to Hive!
# Call command - ./sqoop_import_member_details.sh database-2.cl4c0rtglkdz.ap-south-1.rds.amazonaws.com BankingPrj admin member_details
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
--warehouse-dir /project_input_data
--incremental append \
--check-column member_id \
--last-value 0
