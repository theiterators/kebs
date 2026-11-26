package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.naming.Naming
import pl.iterators.kebs.playjson.KebsPlayJson
import play.api.libs.json._

object PlayCompanionTestDomain {
  object Snake extends KebsPlayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.snakeCase)
  }
  final case class Named(someField: Int, otherField: Int)
  object Named { implicit val format = Snake.deriveFormat[Named] }
  object Protocol extends KebsPlayJson
  import Protocol._
  def inCompanion = Json.toJson(List(Named(1, 2))).toString

  def inMethod = {
    val writes: OWrites[Named] = Snake.deriveWrites[Named]
    writes.writes(Named(1, 2)).toString
  }
  val inValue = {
    val reads: Reads[Named] = Snake.deriveReads[Named]
    reads.reads(Json.obj("some_field" -> 1, "other_field" -> 2)).get
  }
}

class PlayCompanionTests extends AnyFunSuite with Matchers {
  test("formats with an inferred type in companions of other files") {
    PlayAaCompanion.written shouldBe """{"outer":{"inner":{"x":1,"y":2},"n":3},"n":4}"""
    PlayZzCompanion.written shouldBe """{"outer":{"inner":{"x":1,"y":2},"n":3},"n":4}"""
  }

  test("explicitly derived instances next to inferred types") {
    import PlayCompanionTestDomain._
    inCompanion shouldBe """[{"some_field":1,"other_field":2}]"""
    inMethod shouldBe """{"some_field":1,"other_field":2}"""
    inValue shouldBe Named(1, 2)
  }
}
