# Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework
Here, implementing a credit card fraud detection system, by using big data technologies, like Hadoop, Spark, Apache Kafka

## Project Implementation tasks

Task 1: Copy “card_transactions.csv” file from local system to HDFS. 

#### Table creation tasks :

Task 2: Create the “card_transactions” table in MySQL based on the card_transactions.csv file structure.

Task 3: Do a sqoop export to the database for card_transactions.csv and delete the file from HDFS.

Task 4: On “member_score” and “member_details” create a normal hive external table.

Task 5: Create a special “card_transactions” Hbase table managed by Hive.

Task 6: Create a Hbase “lookup” table with columns - member_id, card_id, UCL, timestamp, zipcode, credit_score.

#### Batch Processing tasks:

Task 7: Sqoop import member_score from AWS-RDS to Hive. (Full load import, has to be refreshed every week)

Task 8: Sqoop import member_details from AWS-RDS to Hive. (Incremental load import in append mode based on member_id for every 8hrs)

Task 9: Sqoop import card_transactions to HDFS from MySQL. (This is a one-time full load activity. The card_transactions table will be updated with new transactions while in streaming mode.)

#### Scheduling tasks:

Task 10: Schedule a sqoop import job using Airflow to import member_score from AWS-RDS to Hive on a full-load.

Task 11: Schedule a sqoop import job using Airflow to import member_details from AWS-RDS to Hive on an incremental append mode for every 8hrs.

Integration tasks:

Task 12: Spark-HBase Integration

   1: For populating the card_transactions table.
   
   2: For populating the look_up table.
   
Task 13: Spark-Hive Integration for spark stream processing.

Task 14: Access the hive tables using apache spark and calculate the UCL

#### Streaming tasks:

Task 15: Producer to create the transactions in JSON format, to be added and queued in Kafka topics.

Task 16: Spark structured streaming program as a Consumer that will consume the data from the kafka topics.

Task 17: Retrieve the timestamp and zipcode of the last transaction of each card.

Task 18: Processing in Spark Streaming -

   1 : Validating RULE 1 -> “credit_score > 200”
   
   2 : Validating RULE 2 -> “transaction amount <= UCL”
   
   3 : Validating RULE 3 -> “zipcode distance within threshold”

Task 19: Based on the above rules, the entire transaction along with status should be updated in the card_transactions table.

Task 20: Schedule a job for validating rules by comparing the incoming data from the POS terminals in JSON format with the values in the lookup table.

Task 21: If the transaction was marked genuine, then we need to update the lookup table with the new timestamp and the zipcode.

Task 22: Schedule a job for populating the lookup table


### Task 1: Copy “card_transactions.csv” file from local system to HDFS.
Move file from windows Desktop/Shared to Cloudera Desktop/Shared using Shared folder

Step1: -
Create directory project_input_data
~~~
hadoop fs -mkdir project_input_data
~~~

Step2:-
Remove header from card_transactions.csv file before transferring to HDFS
~~~
sed -i '1d'  Desktop/Shared/card_transactions.csv 
~~~
Step3: -
Moving file from cloudera local to newly created HDFS folder project_input_data
~~~
hadoop fs -put Desktop/card_transactions.csv project_input_data
~~~
Step4: -
Verifying the record count of the card_transactions file loaded into HDFS 
~~~
hadoop fs -cat project_input_data/card_transactions.csv | wc -l
~~~
![Move file from windows Desktop Shared to Cloudera](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/e2d55add-ad59-4cac-acf6-bc96f0ea6b8c)

## Table creation tasks :

### Task 2: Create the “card_transactions” table in MySQL based on the card_transactions.csv file structure.
Step1: -
Login to mysql 
~~~
mysql -u root -p
~~~
Creating database bigdataproject and using same 
~~~
create database bigdataproject;
use bigdataproject;
~~~
![Picture2](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/d07bc295-c541-4bb2-ade2-ce7d146c16ed)

Step2: -
Creating table card_transactions in MySQL based on the card_transactions.csv file structure.
~~~
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
~~~
~~~
show tables;
~~~
![Picture3](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/56835110-c82b-48ba-b79d-227afcb6ed19)
~~~
Describe stg_card_transactions;
~~~
![Picture4](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/6fa9bc03-f0c3-4878-a2d0-d9dd98717717)
~~~
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
~~~
~~~
Describe card_transactions;
~~~
![Picture5](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/0195df7d-31c2-45f0-a410-267fd83e8773)

### Task 3: Do a sqoop export to the database for card_transactions.csv and delete the file from HDFS.
Step1: -

Doing sqoop export of card_transactions data using password encryption from HDFS to MYSQL(staging table)
~~~
hadoop credential create mysql.bigdataproject.password -provider jceks://hdfs/user/cloudera/mysql.dbpassword.jceks
~~~
~~~
sqoop export \
-Dhadoop.security.credential.provider.path=jceks://hdfs/user/cloudera/mysql.dbpassword.jceks \
--connect jdbc:mysql://quickstart.cloudera:3306/bigdataproject \
--username root \
--password-alias mysql.bigdataproject.password \
--table stg_card_transactions  \
--export-dir project_input_data/card_transactions.csv \
--fields-terminated-by ','
~~~
~~~
select count(*) from bigdataproject.stg_card_transactions;
~~~
![Picture6](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/4b2c2297-3aea-464b-bf48-a38ee2e69f32)

step 2:
Transferring data from stg_card_transactions to card_transactions
~~~
insert into card_transactions select card_id,member_id,amount,postcode,pos_id, STR_TO_DATE(transaction_dt,'%d-%m-%Y %H:%i:%s') as transaction_dt,status  from stg_card_transactions
~~~
![Picture7](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/b76f9f6b-3c10-4175-830c-ef98ab38915d)

Step3: -
Providing permission and execution command for sqoop import shell script
~~~
chmod +x sqoop_export_card_transactions.sh
~~~
Shell execution command:-
~~~
./sqoop_export_card_transactions.sh quickstart.cloudera:3306 bigdataproject root card_transactions
~~~
Step4: -
Deleting the file from HDFS
~~~
hadoop fs -rm /project_input_data/card_transactions.csv
~~~
![Picture8](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/b779a5e1-a5f6-4568-8f2f-e1a4db6885bd)

### Task 4: On “member_score” and “member_details” create a normal hive external table.
Step1: -
Enter to Hive Beeline Terminal
~~~
beeline -u jdbc:hive2://
~~~
Create database in Hive 
~~~
create database bigdataproject;
use database bigdataproject;
~~~
Create hive external table- member_score 
~~~
create external table if not exists member_score 
(
 member_id string,
 score float
)
row format delimited fields terminated by ',' 
stored as textfile 
location '/project_input_data/member_score/';
~~~
Step2: -
Create hive external table- member_details 
~~~
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
~~~
~~~
Describe member_score;
~~~
![Picture9](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/32a3d290-6faf-454b-a6bf-020dff3023e1)
~~~
Describe member_details;
~~~
![Picture10](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/fc45930a-e722-4ca5-bab6-cd89c3eb0df8)

Step3: -

Enabling bucketing:- 
~~~
SET HIVE.ENFORCE.BUCKETING=TRUE;
~~~
Create hive bucketed table- member_score with 4 Buckets on column:- member_id since query/join with this table is based on card_id
~~~
create table if not exists member_score_bucketed
(
 member_id string,
 score float
)
CLUSTERED BY (member_id) into 4 buckets;
~~~
Step4: -
Create hive bucketed table- member_details with 4 Buckets on column:- card_id since query/join with this table is based on card_id
~~~
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
~~~

Step5: -
Loading bucketed tables member_score_bucketed and member_details_bucketed from external tables
~~~
insert into table member_score_bucketed
select * from member_score;
~~~
~~~
insert into table member_details_bucketed
select * from member_details;
~~~
### Task 5: Create a special “card_transactions” Hbase table managed by Hive.
Step1:- Creating Hive-HBase table table card_transactions with 8 buckets on column:-card_id since query/join with this table is based on card_id.
~~~
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
~~~
~~~
Describing card_transactions table from hive
~~~
![Picture11](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/ea0658bc-f6f6-49bb-8dce-3555fd3a40dc)
~~~
Describing card_transactions table from HBase
~~~
![Picture12](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/c2549062-07d0-42c0-9d26-e4a6ec4d2b77)

### Task 6: Create a Hbase “lookup” table with columns — member_id, card_id, UCL, timestamp, zipcode, credit_score.

Step1:- Creating Hive-HBase table table card_lookup with 8 buckets on column:-card_id since query/join with this table is based on card_id.
~~~
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
~~~
~~~
Describing card_ lookup table from hive
~~~
![Picture13](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/baa95b12-aa64-497e-961d-9a1e42f78058)
~~~
Describing card_transactions table from HBase
~~~
![Picture14](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/46a65bb0-b215-413e-8c8f-0724fd7ada0c)

## Batch Processing tasks:

### Task 7: Sqoop import member_score from AWS-RDS to Hive. (Full load import, has to be refreshed every week)
Step1: -
Doing sqoop import of member_score data using password encryption from Amazon RDS to HDFS and used delete target directory since it full load.
~~~
hadoop credential create amazonrds.bigdataproject.password -provider jceks://hdfs/user/cloudera/amazonrds.dbpassword.jceks
~~~
~~~
sqoop import \
-Dhadoop.security.credential.provider.path=jceks://hdfs/user/cloudera/amazonrds.dbpassword.jceks \
--connect jdbc:mysql://database-2.cl4c0rtglkdz.ap-south-1.rds.amazonaws.com/BankingPrj \
--username admin \
--password-alias amazonrds.bigdataproject.password \
--table member_score \
-- target -dir /project_input_data/member_score \
--delete-target-dir
~~~
~~~
select count(*) from bigdataproject.member_score;
~~~
![Picture15](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/66435506-f67b-4bcf-9077-dcf62d7da4fc)

Step2: -
Production ready parameterized Sqoop import Shell Script(password encrypted) for Amazon RDS member_score table

Step3: -
Providing permission and execution command for sqoop import shell script
~~~
chmod +x sqoop_import_member_score.sh
~~~
Shell execution command:-
~~~
./sqoop_import_member_score.sh database-2.cl4c0rtglkdz.ap-south-1.rds.amazonaws.com BankingPrj admin member_score
~~~
### Task 8:Sqoop import member_details from AWS-RDS to Hive. (Incremental load import in append mode based on member_id for every 8hrs)

Step1: -
Doing sqoop import of member_details data using password encryption from Amazon RDS to HDFS using incremental append mode based on column:- member_id

We can reuse the password encryption file created for member_score table for member_details too
~~~
sqoop import \
-Dhadoop.security.credential.provider.path=jceks://hdfs/user/cloudera/amazonrds.dbpassword.jceks \
--connect jdbc:mysql://database-2.cl4c0rtglkdz.ap-south-1.rds.amazonaws.com/BankingPrj \
--username admin \
--password-alias amazonrds.bigdataproject.password \
--table member_details \
--warehouse-dir /project_input_data \
--incremental append \
--check-column member_id \
--last-value 0
~~~
~~~
select count(*) from bigdataproject.member_details;
~~~
![Picture17](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/293d19b6-ec65-4e99-a9d2-61dcf8795d89)

Step2: -
Production ready parameterized Sqoop import Shell Script(password encrypted) for Amazon RDS member_details table

Step3: -
Providing permission and execution command for sqoop import shell script
~~~
chmod +x sqoop_import_member_details.sh
~~~
Shell execution command:-
~~~
./sqoop_import_member_score.sh database-2.cl4c0rtglkdz.ap-south-1.rds.amazonaws.com BankingPrj admin member_details
~~~
### Task 9: Sqoop import card_transactions to HDFS from MySQL. (This is a one-time full load activity. The card_transactions table will be updated with new transactions while in streaming mode.)

Step1: -
Doing sqoop import of card_transactions data using password encryption from MYSQL to HDFS
~~~
sqoop import \
-Dhadoop.security.credential.provider.path=jceks://hdfs/user/cloudera/mysql.dbpassword.jceks \
--connect jdbc:mysql://quickstart.cloudera:3306/bigdataproject \
--username root \
--password-alias mysql.bigdataproject.password \
--table card_transactions  \
--split-by card_id \
--warehouse-dir /project_input_data \
~~~
~~~
hadoop fs -ls /project_input_data/card_transactions
~~~
![Picture19](https://github.com/laijupjoy/Real-Time-Credit-Card-Fraud-Transaction-Detection-system-using-Big-Data-Framework/assets/87544051/3dd7c369-a245-4dd7-a98c-f0a655c0369d)

Step2: - 
Production ready parameterized Sqoop import Shell Script(password encrypted) for MYSQL card_transactions table

Step3: -
Providing permission and execution command for sqoop import shell script
~~~
chmod +x sqoop_import_card_txns.sh
~~~
Shell execution command:-
~~~
./sqoop_import_card_txns.sh quickstart.cloudera:3306 bigdataproject root card_transactions
~~~

## Integration tasks:

### Task 13 & 14 Combined: Spark-Hive Integration for spark stream processing & Access the hive tables using apache spark and calculate the UCL.

Step1: -
Access the hive tables using apache spark and calculate the UCL.
~~~
import org.apache.spark.sql.{Row, SaveMode, SparkSession}
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.SparkConf
object HiveTest extends App{
//set logging level to error
  Logger.getLogger("org").setLevel(Level.ERROR)
 // create spark config object
  val sparkConf= new SparkConf()
  sparkConf.setAppName("Credit_Card_Fraud_Detection")
  sparkConf.setMaster("local[2]")
  sparkConf.set("hive.metastore.uris", "thrift://localhost:9083")  
  // use spark config object to create spark session
  val spark = SparkSession
  .builder()
  .config(sparkConf)
  .enableHiveSupport() //spark integration with hive
  .getOrCreate()
	import spark.implicits._
	import spark.sql	
	

// start writing the hive queries
	val df_ucl=sql("""
	with cte_rownum as
	(
		select card_id,amount,member_id, 
		first_value(postcode) over(partition by card_id order by transcation_dt desc) as postcode,
		row_number() over(partition by card_id order by transcation_dt desc) rownum
		from card_transactions
	)
	select card_id,member_id,
			avg(amount)+ 3* max(std) as UCL ,
			max(score) score,
			max(transaction_dt) as last_txn_time,
			max(Postcode)as last_txn_zip	
			 from
			(
				select
				card_id,amount,
				c.member_id,
				m.score,
				transaction_dt,
				Postcode,
				STDDEV (amount) over(partition by card_id order by (select 1)  desc) std
				from cte_rownum c
				inner join member_score m on c.member_id=m.member_id 
				where rownum<=10
			)a
	group by card_id,member_id
	""" )
	val df=df_ucl.select("card_id","member_id","UCL","score","last_txn_time","last_txn_zip")
	df.show

}
~~~


Reference:https://ijarsct.co.in/Paper8040.pdf






















 


