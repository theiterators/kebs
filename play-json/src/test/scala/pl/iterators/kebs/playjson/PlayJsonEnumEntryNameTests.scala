package pl.iterators.kebs.playjson

import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.enumeratum.KebsEnumeratum
import play.api.libs.json._

object PlayJsonEnumEntryNameTests {
  sealed trait Status extends EnumEntry
  object Status       extends Enum[Status] {
    case object Active   extends Status { override val entryName = "is-active" }
    case object Inactive extends Status
    val values = findValues
  }
}

class PlayJsonEnumEntryNameTests extends AnyFunSuite with Matchers with KebsEnumeratum {
  import PlayJsonEnumEntryNameTests._

  test("enum Reads / Writes use entryName") {
    import pl.iterators.kebs.playjson.enums._
    Json.toJson[Status](Status.Active) shouldBe JsString("is-active")
    Json.fromJson[Status](JsString("IS-ACTIVE")) shouldBe JsSuccess(Status.Active)
    Json.fromJson[Status](JsString("Active")) shouldBe JsError("Active should be one of is-active, Inactive")
  }

  test("enum Reads / Writes use entryName - uppercase") {
    import pl.iterators.kebs.playjson.enums.uppercase._
    Json.toJson[Status](Status.Active) shouldBe JsString("IS-ACTIVE")
    Json.fromJson[Status](JsString("IS-ACTIVE")) shouldBe JsSuccess(Status.Active)
  }

  test("enum Reads / Writes use entryName - lowercase") {
    import pl.iterators.kebs.playjson.enums.lowercase._
    Json.toJson[Status](Status.Active) shouldBe JsString("is-active")
    Json.fromJson[Status](JsString("is-active")) shouldBe JsSuccess(Status.Active)
  }
}
