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

#### Table creation tasks :

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






 


