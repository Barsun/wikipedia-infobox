package xml.dump

import info.bliki.wiki.dump.{IArticleFilter, Siteinfo, WikiArticle}
import org.xml.sax.SAXException


class SetterArticleFilter(val wrappedPage: WrappedPage) extends IArticleFilter {
  @throws(classOf[SAXException])
  def process(page: WikiArticle, siteinfo: Siteinfo) {
    wrappedPage.page = page
  }
}

/**
  * A helper class that allows for a WikiArticle to be serialized and also pulled from the XML parser
  *
  * @param page The WikiArticle that is being wrapped
  */
case class WrappedPage(var page: WikiArticle = new WikiArticle)

case class PageInfoBox(pageId: String, title: String, infoBox: String)