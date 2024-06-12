# Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework
Here, implementing a credit card fraud detection system, by using big data technologies, like Hadoop, Spark, Apache Kafka

Reference:https://ijarsct.co.in/Paper8040.pdf

### Project Implementation tasks
#### Task 1: Copy “card_transactions.csv” file from local system to HDFS.
Move file from windows Desktop/Shared to Cloudera Desktop/Shared using Shared folder

Step1: -
Create directory project_input_data
hadoop fs -mkdir project_input_data

Step2:-
Remove header from card_transactions.csv file before transferring to HDFS
sed -i '1d'  Desktop/Shared/card_transactions.csv 

Step3: -
Moving file from cloudera local to newly created HDFS folder project_input_data
hadoop fs -put Desktop/card_transactions.csv project_input_data

Step4: -
Verifying the record count of the card_transactions file loaded into HDFS 
hadoop fs -cat project_input_data/card_transactions.csv | wc -l

![Move file from windows Desktop Shared to Cloudera](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/e2d55add-ad59-4cac-acf6-bc96f0ea6b8c)

### Table creation tasks :

#### Task 2: Create the “card_transactions” table in MySQL based on the card_transactions.csv file structure.
Step1: -
Login to mysql 
mysql -u root -p

Creating database bigdataproject and using same 
create database bigdataproject;
use bigdataproject;

![Picture2](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/d07bc295-c541-4bb2-ade2-ce7d146c16ed)

Step2: -
Creating table card_transactions in MySQL based on the card_transactions.csv file structure.

create table stg_card_transactions (
card_id bigint,
member_id bigint,
amount int,
postcode int,
pos_id bigint,
transaction_dt varchar(255),
status varchar(50),
PRIMARY KEY (card_id ,transaction_dt)
);

show tables;

![Picture3](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/56835110-c82b-48ba-b79d-227afcb6ed19)

Describe stg_card_transactions;

![Picture4](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/6fa9bc03-f0c3-4878-a2d0-d9dd98717717)

create table card_transactions (
card_id bigint,
member_id bigint,
amount int,
postcode int,
pos_id bigint,
transaction_dt datetime,
status varchar(50),
PRIMARY KEY (card_id ,transaction_dt)
);

Describe card_transactions;

![Picture5](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/0195df7d-31c2-45f0-a410-267fd83e8773)

#### Task 3: Do a sqoop export to the database for card_transactions.csv and delete the file from HDFS.
Step1: -

Doing sqoop export of card_transactions data using password encryption from HDFS to MYSQL(staging table)

hadoop credential create mysql.bigdataproject.password -provider jceks://hdfs/user/cloudera/mysql.dbpassword.jceks

sqoop export \
-Dhadoop.security.credential.provider.path=jceks://hdfs/user/cloudera/mysql.dbpassword.jceks \
--connect jdbc:mysql://quickstart.cloudera:3306/bigdataproject \
--username root \
--password-alias mysql.bigdataproject.password \
--table stg_card_transactions  \
--export-dir project_input_data/card_transactions.csv \
--fields-terminated-by ','

select count(*) from bigdataproject.stg_card_transactions;

![Picture6](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/4b2c2297-3aea-464b-bf48-a38ee2e69f32)

step 2:
Transferring data from stg_card_transactions to card_transactions

insert into card_transactions select card_id,member_id,amount,postcode,pos_id, STR_TO_DATE(transaction_dt,'%d-%m-%Y %H:%i:%s') as transaction_dt,status  from stg_card_transactions

![Picture7](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/b76f9f6b-3c10-4175-830c-ef98ab38915d)

Step3: -
Providing permission and execution command for sqoop import shell script
chmod +x sqoop_export_card_transactions.sh

Shell execution command:-
./sqoop_export_card_transactions.sh quickstart.cloudera:3306 bigdataproject root card_transactions

Step4: -
Deleting the file from HDFS
hadoop fs -rm /project_input_data/card_transactions.csv

![Picture8](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/b779a5e1-a5f6-4568-8f2f-e1a4db6885bd)

#### Task 4: On “member_score” and “member_details” create a normal hive external table.
Step1: -
Enter to Hive Beeline Terminal
beeline -u jdbc:hive2://

Create database in Hive 
create database bigdataproject;
use database bigdataproject;

Create hive external table- member_score 

create external table if not exists member_score 
(
 member_id string,
 score float
)
row format delimited fields terminated by ',' 
stored as textfile 
location '/project_input_data/member_score/';

Step2: -
Create hive external table- member_details 

create external table if not exists member_details 
(
card_id bigint,
member_id bigint,
member_joining_dt timestamp ,
card_purchase_dt timestamp ,
country string,
city string,
score float
)
row format delimited fields terminated by ',' 
stored as textfile 
location '/project_input_data/member_details/';

Describe member_score;

![Picture9](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/32a3d290-6faf-454b-a6bf-020dff3023e1)

Describe member_details;

![Picture10](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/fc45930a-e722-4ca5-bab6-cd89c3eb0df8)

Step3: -

Enabling bucketing:- 
SET HIVE.ENFORCE.BUCKETING=TRUE;

Create hive bucketed table- member_score with 4 Buckets on column:- member_id since query/join with this table is based on card_id

create table if not exists member_score_bucketed
(
 member_id string,
 score float
)
CLUSTERED BY (member_id) into 4 buckets;

Step4: -
Create hive bucketed table- member_details with 4 Buckets on column:- card_id since query/join with this table is based on card_id

create table if not exists member_details_bucketed
(
card_id bigint,
member_id bigint,
member_joining_dt timestamp ,
card_purchase_dt timestamp ,
country string,
city string,
score float
)
CLUSTERED BY (card_id) into 4 buckets;


Step5: -
Loading bucketed tables member_score_bucketed and member_details_bucketed from external tables

insert into table member_score_bucketed
select * from member_score;

insert into table member_details_bucketed
select * from member_details;

#### Task 5: Create a special “card_transactions” Hbase table managed by Hive.
Step1:- Creating Hive-HBase table table card_transactions with 8 buckets on column:-card_id since query/join with this table is based on card_id.

create table card_transactions 
(
card_id bigint,
member_id bigint,
amount float,
postcode int,
pos_id bigint,
transaction_dt timestamp,
status string
)
CLUSTERED by (card_id) into 8 buckets
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler' 
WITH SERDEPROPERTIES("hbase.columns.mapping"=":key,trans_data:member_id,trans_data:amount, trans_data:postcode,trans_data:pos_id,trans_data:transaction_dt,trans_data:Status")
TBLPROPERTIES ("hbase.table.name" = "card_transactions");

Describing card_transactions table from hive

![Picture11](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/ea0658bc-f6f6-49bb-8dce-3555fd3a40dc)

Describing card_transactions table from HBase

![Picture12](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/c2549062-07d0-42c0-9d26-e4a6ec4d2b77)

#### Task 6: Create a Hbase “lookup” table with columns — member_id, card_id, UCL, timestamp, zipcode, credit_score.

Step1:- Creating Hive-HBase table table card_lookup with 8 buckets on column:-card_id since query/join with this table is based on card_id.

create table card_lookup
 (
card_id bigint ,
member_id bigint ,
ucl float ,
score float,
last_txn_time timestamp,
last_txn_zip string 
)
CLUSTERED by (card_id) into 8 buckets
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler' 
WITH SERDEPROPERTIES("hbase.columns.mapping"=":key,lkp_data:ucl,lkp_data:score, lkp_data:last_txn_time,lkp_data:last_txn_zip")
TBLPROPERTIES ("hbase.table.name" = "card_lookup");

Describing card_ lookup table from hive

![Picture13](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/baa95b12-aa64-497e-961d-9a1e42f78058)

Describing card_transactions table from HBase

![Picture14](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/46a65bb0-b215-413e-8c8f-0724fd7ada0c)

### Batch Processing tasks:

### Task 7: Sqoop import member_score from AWS-RDS to Hive. (Full load import, has to be refreshed every week)
Step1: -
Doing sqoop import of member_score data using password encryption from Amazon RDS to HDFS and used delete target directory since it full load.

hadoop credential create amazonrds.bigdataproject.password -provider jceks://hdfs/user/cloudera/amazonrds.dbpassword.jceks

sqoop import \
-Dhadoop.security.credential.provider.path=jceks://hdfs/user/cloudera/amazonrds.dbpassword.jceks \
--connect jdbc:mysql://database-2.cl4c0rtglkdz.ap-south-1.rds.amazonaws.com/BankingPrj \
--username admin \
--password-alias amazonrds.bigdataproject.password \
--table member_score \
-- target -dir /project_input_data/member_score \
--delete-target-dir


select count(*) from bigdataproject.member_score;

![Picture15](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/66435506-f67b-4bcf-9077-dcf62d7da4fc)

Step2: -
Production ready parameterized Sqoop import Shell Script(password encrypted) for Amazon RDS member_score table

![Picture16](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/da2465ac-d63e-4a7c-8985-ba5397d80926)

Step3: -
Providing permission and execution command for sqoop import shell script
chmod +x sqoop_import_member_score.sh

Shell execution command:-
./sqoop_import_member_score.sh database-2.cl4c0rtglkdz.ap-south-1.rds.amazonaws.com BankingPrj admin member_score














 


