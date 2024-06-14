import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.StringType
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.types.DoubleType

object Hive_Integration2 extends App{
   Logger.getLogger("org").setLevel(Level.ERROR)
  
  val spark=SparkSession.builder().master("local[2]").appName("streamingApp").getOrCreate();
  
  val tranxns=spark.readStream.format("kafka")
  .option("kafka.bootstrap.servers", "localhost:9092")
  .option("subscribe", "mynewtopic")
  .option("startingOffsets", "latest")
  .load()

  
 val tranxnsString = tranxns.selectExpr("CAST(value AS STRING)","timestamp")
 
 val schema = new StructType()
      .add("card_id",LongType)
      .add("member_id",LongType)
      .add("amount",DoubleType)
      .add("pos_id",IntegerType)
      .add("post_code",IntegerType)
      .add("transc_dt",StringType)
      .add("status",StringType)
      
    val tranxnsDF = tranxnsString.select(from_json(col("value"), schema).alias("value"),col("timestamp")).select("value.*","timestamp")
   
    tranxnsDF.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
}

//{"card_id": 917221245657777,"member_id": 37495064475648584,"amount": 9000.567,"pos_id": 33946,"post_code":3946,"transc_dt":"11/02/2020","status":"GENUINE"}
