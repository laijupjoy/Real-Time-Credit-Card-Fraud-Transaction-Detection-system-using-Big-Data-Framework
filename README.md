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

 


