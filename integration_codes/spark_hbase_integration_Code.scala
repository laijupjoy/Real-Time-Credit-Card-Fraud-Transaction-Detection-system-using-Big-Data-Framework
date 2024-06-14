import org.apache.spark.sql.{ Row, SaveMode, SparkSession }
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{
  HBaseAdmin,
  Result,
  Put,
  HTable,
  ConnectionFactory,
  Connection,
  Get,
  Scan
}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.util.Bytes
object sparkHiveHbaseInt2 extends App {
  //set logging level to error
  Logger.getLogger("org").setLevel(Level.ERROR)
  // create spark config object
  val sparkConf = new SparkConf()
  sparkConf.setAppName("Credit_Card_Fraud_Detection")
  sparkConf.setMaster("local[2]")
  sparkConf.set("hive.metastore.uris", "thrift://localhost:9083")
  // use spark config object to create spark session
  val spark = SparkSession
    .builder()
    .config(sparkConf)
    .enableHiveSupport()
    .getOrCreate()

  val conf = HBaseConfiguration.create()

  conf.set("hbase.zookeeper.quoram", "localhost")
  conf.set("hbase.zookeeper.property.clientPort", "2181")
  val connection: Connection = ConnectionFactory.createConnection(conf)
  val tableName = connection.getTable(TableName.valueOf("card_lookup"))

  //SET hive.metastore.schema.verification=true;
  import spark.implicits._
  import spark.sql
  // start writing the hive queries
  val df_ucl = sql("""
			with cte_rownum as
			(
			select card_id,amount,member_id,transaction_dt,
			first_value(postcode) over(partition by card_id order by transaction_dt desc) as postcode,
			row_number() over(partition by card_id order by transaction_dt desc) rownum
			from bigdataproject.card_transactions
			)
			select card_id,member_id,
			round((avg(amount)+ 3* max(std)),0) as ucl ,
			max(score) score,
			max(transaction_dt) as last_txn_time,
			max(Postcode)as last_txn_zip	
			from
			(	select
			card_id,amount,
			c.member_id,
			m.score,
			c.transaction_dt,
			Postcode,
			STDDEV (amount) over(partition by card_id order by (select 1)  desc) std
			from cte_rownum c
			inner join bigdataproject.member_score_bucketed m on c.member_id=m.member_id 
			where rownum<=10
			)a
			group by card_id,member_id
			""")
  val df = df_ucl.select("card_id", "member_id", "ucl", "score", "last_txn_time", "last_txn_zip")

  //df.write.mode("append").saveAsTable("card_lookup")
  //println(sql("""select * from card_lookup""").count())

  println("Dataframe extract is completed");

df.foreach { myRow =>
	  {
  var myArray = myRow.mkString(",").split(",") 
  
    var cardId = myArray(0)
    var memberId = myArray(1)
    var ucl  = myArray(2)
    var score  = myArray(3)
    var lastTxnTime = myArray(4)
    var lastTxnZip = myArray(5)
    val row = new Put(Bytes.toBytes(cardId))
    row.addColumn(Bytes.toBytes("lkp_data"),Bytes.toBytes("member_id"),Bytes.toBytes(memberId))
    row.addColumn(Bytes.toBytes("lkp_data"),Bytes.toBytes("ucl"),Bytes.toBytes(ucl))
    row.addColumn(Bytes.toBytes("lkp_data"),Bytes.toBytes("score"),Bytes.toBytes(score))
    row.addColumn(Bytes.toBytes("lkp_data"),Bytes.toBytes("last_txn_time"),Bytes.toBytes(lastTxnTime))
    row.addColumn(Bytes.toBytes("lkp_data"),Bytes.toBytes("last_txn_zip"),Bytes.toBytes(lastTxnZip))   
    tableName.put(row) 
	  }
}
  tableName.close()
  connection.close()
  println("Success");

}

