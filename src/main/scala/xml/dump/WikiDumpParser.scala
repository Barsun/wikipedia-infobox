package xml.dump

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime

import scala.io.Source
import scala.util.Try


object WikiDumpParser extends App {

  if (args.isEmpty) {
    sys.error("specify input file name")
  }

  // Input parameters
  val inputXmlFile = args(0)
  val outputLocation = new File(args(1))
  val infoBoxNames =
    if (args.isDefinedAt(2)) args(2).split(",").map(_.trim).toSet
    else sys.error("Specify 3rd argument with comma separated infoBox names")
  val outDirPrefix =
    if (args.isDefinedAt(3)) args(3).trim
    else LocalDateTime.now().toString

  if (outputLocation.isFile) {
    sys.error(s"Output must be a directory, but was '${outputLocation.getAbsolutePath}'")
  }

  createDir(outputLocation)

  val lastSeenPageId = if (Files.exists(Paths.get("lastSeenPageId.txt"))) {
    Source.fromFile("lastSeenPageId.txt").getLines().toList.headOption.map(_.trim).filter(s => Try(s.toLong).isSuccess)
  } else None

  lastSeenPageId.foreach(l => println(s"Current lastSeenPageId: $l"))
  PageParser(outputLocation, outDirPrefix).parseInfoBoxToCsv(inputXmlFile, infoBoxNames, lastSeenPageId)

  private def createDir(dir: File) = {
    if (!dir.exists()) {
      println("Creating output directory: " + dir.getAbsolutePath)
      dir.mkdirs()
    }
  }
}
