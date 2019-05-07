package xml

import org.scalatest.{FlatSpec, Matchers}
import xml.infobox.InfoBoxPropertiesParser

import scala.io.Source

class SettlementParserTest extends FlatSpec with Matchers {

  it should "parse 'settlement' infobox into map" in {
    //given
    val infoBox = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("detroit_settlement.txt")).mkString
    //when
    val properties = InfoBoxPropertiesParser.parse(infoBox)
    //then
    properties should contain allOf(
      "name" -> "Detroit",
      "1" -> "[[Charles Pugh]] – Council President",
      "footnotes" -> "",
      "population_rank" -> "[[List of United States cities by population|18th in U.S.]]"
    )
  }
}
