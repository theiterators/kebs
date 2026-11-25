package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import spray.json._

object SprayCompanionTestDomain {
  import SprayAaCompanion.Snake.jsonFormatN
  import SprayAaCompanion.Protocol.IntJsonFormat

  final case class Named(someField: Int, otherField: Int)

  def inMethod = {
    val format: RootJsonFormat[Named] = jsonFormatN[Named]
    format.write(Named(1, 2)).compactPrint
  }
  val inValue = {
    implicit val format: RootJsonFormat[Named] = jsonFormatN[Named]
    Named(1, 2).toJson.compactPrint
  }
  lazy val inLazyValue = {
    implicit val format = jsonFormatN[Named]
    Named(1, 2).toJson.compactPrint
  }
}

class SprayCompanionTests extends AnyFunSuite with Matchers {
  test("formats with an inferred type in companions of other files") {
    SprayAaCompanion.written shouldBe """{"n":4,"outer":{"inner":{"x":1,"y":2},"n":3}}"""
    SprayZzCompanion.written shouldBe """{"n":4,"outer":{"inner":{"x":1,"y":2},"n":3}}"""
  }

  test("explicitly derived formats with an inferred type in companions of other files") {
    SprayAaCompanion.writtenNamed shouldBe """{"n":3,"named":{"other_field":2,"some_field":1}}"""
    SprayZzCompanion.writtenNamed shouldBe """{"n":3,"named":{"other_field":2,"some_field":1}}"""
  }

  test("explicitly derived formats inside definitions with an inferred type") {
    val json = """{"other_field":2,"some_field":1}"""
    SprayCompanionTestDomain.inMethod shouldBe json
    SprayCompanionTestDomain.inValue shouldBe json
    SprayCompanionTestDomain.inLazyValue shouldBe json
  }
}
