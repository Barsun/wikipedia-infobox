package xml.dump

import java.io.{ByteArrayInputStream, File, FileOutputStream}
import java.nio.file.{Files, Paths}

import info.bliki.wiki.dump.{WikiPatternMatcher, WikiXMLParser}
import xml.dump.PageParser.infoBoxPrefix

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.control.NonFatal
import scala.xml.XML
import scala.xml.pull.{EvElemEnd, EvElemStart, EvText, XMLEventReader}

object PageParser {
  val infoBoxPrefix = "{{Infobox"

  def apply(outputLocation: File, outDirPrefix: String) =
    new PageParser(outputLocation, outDirPrefix)
}

class PageParser(outputLocation: File, outDirPrefix: String) {

  def parseInfoBoxToCsv(
                         inputXmlFileName: String,
                         infoBoxFilter: Set[String],
                         lastSeenPageId: Option[String] = None): Unit = {

    def nonEmptyInfoBox(page: PageInfoBox, infoBox: String) =
      page.infoBox.trim != s"$infoBoxPrefix $infoBox}}"

    val infoBoxToDirName = infoBoxFilter.map(n => n -> (outDirPrefix + "-" + n)).toMap

    parseXml(inputXmlFileName, page => {
      println(s"processing pageId: ${page.pageId} ")

      if (lastSeenPageId.isEmpty || lastSeenPageId.exists(_.compare(page.pageId.trim) < 0)) {
        infoBoxFilter.foreach { infoBoxName =>

          if (page.infoBox.startsWith(s"$infoBoxPrefix $infoBoxName")
            && nonEmptyInfoBox(page, infoBoxName)) {
            println(s"found $infoBoxName, going to save a page with id: ${page.pageId}")
            writeInfoBox(infoBoxToDirName(infoBoxName), page.pageId, page.infoBox)
          }
        }
      }
    })
  }

  private def parseXml(inputXmlFileName: String, callback: PageInfoBox => Unit): Unit = {
    val xml = new XMLEventReader(Source.fromFile(inputXmlFileName))
    var insidePage = false
    var buf = ArrayBuffer[String]()

    for (event <- xml) {
      event match {
        case EvElemStart(_, "page", _, _) =>
          insidePage = true
          val tag = "<page>"
          buf += tag
        case EvElemEnd(_, "page") =>
          val tag = "</page>"
          buf += tag
          insidePage = false

          parsePageInfoBox(buf.mkString).foreach(callback)
          buf.clear
        case e@EvElemStart(_, tag, _, _) =>
          if (insidePage) {
            buf += ("<" + tag + ">")
          }
        case e@EvElemEnd(_, tag) =>
          if (insidePage) {
            buf += ("</" + tag + ">")
          }
        case EvText(t) =>
          if (insidePage) {
            buf += t
          }
        case _ => // ignore
      }
    }
  }

  private def parsePageInfoBox(text: String): Option[PageInfoBox] = {
    val maybeInfoBox = Option(new WikiPatternMatcher(text).getInfoBox).map(_.dumpRaw())

    maybeInfoBox.flatMap { infoBox =>
      val wrappedPage = new WrappedPage
      //The parser occasionally throws exceptions out, we ignore these
      try {
        val parser = new WikiXMLParser(
          new ByteArrayInputStream(text.getBytes),
          new SetterArticleFilter(wrappedPage))
        parser.parse()
      } catch {
        case e: Exception => //ignore
      }

      val page = wrappedPage.page

      if (page.getText != null && page.getTitle != null && page.getId != null
        && page.getRevisionId != null && page.getTimeStamp != null
        && !page.isCategory && !page.isTemplate) {
        val pageId = {
          val textElem = XML.loadString(text)
          (textElem \ "id").head.child.head.toString
        }
        Some(PageInfoBox(pageId, page.getTitle, infoBox))
      } else {
        None
      }
    }
  }

  private def writeInfoBox(infoBoxName: String, pageId: String, text: String): Unit = {
    val path = Paths.get(outputLocation.toString, infoBoxName)
    Files.createDirectories(path)
    val fullPath = path.resolve(pageId + ".txt").toAbsolutePath.toFile

    println(s"writing to: $fullPath")
    val out = new FileOutputStream(fullPath)

    try {
      out.write(text.getBytes())
    } catch {
      case NonFatal(throwable) =>
        sys.error(s"error '${throwable.getMessage}' while saving page $fullPath")
    } finally out.close()
  }
}
