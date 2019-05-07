package xml.infobox

import java.io.{File, FileWriter}
import java.nio.file.Files

import org.apache.spark.{SparkConf, SparkContext}


object SparkReducer extends App {
  val infoBoxPropsMap = Map(
    "person" -> Person.properties,
    "settlement" -> Settlement.properties,
    "ort" -> Ort.properties,
    "writer" -> Writer.properties
  )

  // Parse input parameters
  val inputLocation = new File(args(0))
  val outputLocation = new File(args(1))

  val infoBoxProps =
    if (args.isDefinedAt(2) && infoBoxPropsMap.isDefinedAt(args(2)))
      infoBoxPropsMap(args(2))
    else
      sys.error(s"Invalid infoBox name: ${args(2)}, available: ${infoBoxPropsMap.keys}")

  if (!inputLocation.isDirectory) {
    sys.error("Input must be a directory: " + inputLocation.getAbsolutePath)
  }

  // Start Spark
  val conf = new SparkConf().setAppName("WikiInfoBoxReducer").setMaster("local[*]")
  val sc = new SparkContext(conf)

  Files.createDirectories(outputLocation.toPath)

  val minimumPropertiesCount = 1

  // Process files

  sc.wholeTextFiles(inputLocation.toString)
    .map { case (path, content) => path -> InfoBoxPropertiesParser.parse(content) }
    .filter { case (_, parsedProps) => parsedProps.size > minimumPropertiesCount }
    .map { case (path, parsedProps) =>
      val pageId = InfoBoxes.pathKey -> path.split("/").last.split("\\.").head
      val values = (infoBoxProps ++ parsedProps.filterKeys(k => infoBoxProps.contains(k)) + pageId).values
      pageId._2 -> values.mkString(",")
    }
    .foreach { case (pageId, content) =>
      val writer = new FileWriter(s"$outputLocation/$pageId.csv")
      writer.write(content)
      writer.close()
    }

  sc.stop()
}


