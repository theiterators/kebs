package pl.iterators.kebs.baklava

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.baklava._
import pl.iterators.kebs.core.enums.EnumNaming
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import pl.iterators.kebs.testkit.model.Color

class BaklavaEnumNamingTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import BaklavaEnumsTests.BaklavaInstances._

  test("enum schema and params use the EnumNaming in scope") {
    import pl.iterators.kebs.baklava.params.enums._
    import pl.iterators.kebs.baklava.schema.enums._
    implicit val enumNaming: EnumNaming = EnumNaming.screamingSnakeCase
    implicitly[Schema[Color]].`enum` shouldBe Some(Set("RED", "DARK_BLUE"))
    implicitly[ToQueryParam[Color]].apply(Color.DarkBlue) shouldBe Seq("DARK_BLUE")
    implicitly[ToPathParam[Color]].apply(Color.DarkBlue) shouldBe "DARK_BLUE"
    implicitly[ToHeader[Color]].unapply("dark_blue") shouldBe Some(Color.DarkBlue)
  }
}
