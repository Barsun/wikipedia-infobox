package xml.infobox

object InfoBoxPropertiesParser {
  val commentPrefix = "!--"

  /**
    *
    * @param text the InfoBox body itself
    * @return Map of names and their values
    */
  def parse(text: String): Map[String, String] = {
    val lines = text.split("\n").drop(1).dropRight(1)
    lines
      .filterNot(l => l.trim.length == 0 || l.startsWith(commentPrefix))
      .map(l => {
        val property = l.split('=')
        val key = property(0).dropWhile(c => c == ' ' || c == '|').trim.toLowerCase
        val value = if (property.isDefinedAt(1)) property(1).trim else ""
        val cleanedValue = if (value.startsWith(commentPrefix)) "" else value
        val escapedValue = if (cleanedValue.contains(",")) s""""$cleanedValue"""" else cleanedValue
        key -> escapedValue
      }).toMap
  }
}