package xml.analytics

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Reports extends App {
  val spark = SparkSession.builder().appName("WikiInfoboxReports").config("spark.master", "local[*]").getOrCreate()

  import spark.implicits._

  val personDf = spark.read.options(Map(
    "header" -> "true",
    "ignoreLeadingWhiteSpace" -> "true",
    "ignoreTrailingWhiteSpace" -> "true",
    "inferSchema" -> "true" //,
    //    "mode" -> "FAILFAST"
    //    "mode" -> "DROPMALFORMED"
  )).csv("/Users/alexey/Downloads/wikipedia/output/person.csv")

  val nationality = personDf.col("nationality")
  val nationalityProjection = when(
    nationality
      .isin("American", "United States", "U.S.", "[[United States|American]]", "[[Americans|American]]",
        "[[United States]]", "USA", "{{USA}}"), "American")
    .otherwise(nationality)
    .as("nationality_new")

  personDf.groupBy(nationalityProjection).count().orderBy("count").show(truncate = false, numRows = 100)


  val settlementDf = spark.read.options(Map(
    "header" -> "true",
    "ignoreLeadingWhiteSpace" -> "true",
    "ignoreTrailingWhiteSpace" -> "true",
    "inferSchema" -> "true"))
    .csv("/Users/alexey/Downloads/wikipedia/settlement.csv")


  val subdivisionName = col("subdivision_name")
  val subdivisionNameProjection = when(subdivisionName.isin("USA", "United States"), "USA")
    .when(subdivisionName.contains("POL").or(subdivisionName.equalTo("Poland")), "Poland")
    .otherwise(subdivisionName)
    .as("subdivision_name_normalized")

  personDf
    .filter(subdivisionName.isNotNull)
    //.map(row => row.getAs[String]("subdivision_name").replace("{{", ""))
    .withColumn("subdivision_name", regexp_replace(col("subdivision_name"), "{{|}}", ""))
    .groupBy(subdivisionName)
    .count()
    .orderBy($"count".desc)
  //.show(truncate = false, numRows = 100)

  val birthDate = col("birth_date")
  personDf
    .select(birthDate)
    .filter(birthDate.isNotNull)
    .withColumn("birthDate_normalized", regexp_replace(col("birthDate_normalized"), "[0-9]4", ""))
    .take(100)
}
