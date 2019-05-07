package xml.infobox

import org.scalatest.{FlatSpec, Matchers}

class InfoBoxesTest extends FlatSpec with Matchers {

  it should "parse infoBoxes properties from the config file" in {
    Person.properties should contain allOf(
      "name" -> "",
      "native_name_lang" -> "",
      "known_for" -> "",
      "children" -> ""
    )
  }

}
