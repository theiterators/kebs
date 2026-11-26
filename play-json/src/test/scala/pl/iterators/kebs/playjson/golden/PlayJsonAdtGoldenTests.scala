package pl.iterators.kebs.playjson.golden

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.playjson.enums.{KebsPlayJsonEnums, KebsPlayJsonValueEnums}
import pl.iterators.kebs.testkit.model._
import play.api.libs.json._

object PlayJsonAdtGoldenTestDomain {
  sealed trait Mark
  final case class Dot(size: Double)    extends Mark
  final case class Line(a: Int, b: Int) extends Mark
  sealed trait Animal
  sealed trait Pet                         extends Animal
  final case class Dog(name: String)       extends Pet
  final case class Wolf(pack: Int, a: Int) extends Animal
  final case class Zoo(animals: List[Animal], favourite: Option[Animal])
}

// same models and JSON as the circe and spray-json ADT goldens: every JSON module writes ADTs the same way
class PlayJsonAdtGoldenTests extends AnyFunSuite with Matchers {
  import PlayJsonAdtGoldenTestDomain._

  object Default       extends KebsPlayJson
  object Flat          extends KebsPlayJson with CaseClass1ToValueClass
  object Discriminated extends KebsPlayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withDiscriminator("type")
  }
  object SnakeConstructors extends KebsPlayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withConstructorNaming(NamingStyle.snakeCase)
  }
  object Enums extends KebsPlayJson with KebsEnumeratum with KebsValueEnumeratum with KebsPlayJsonEnums with KebsPlayJsonValueEnums

  private def roundTrip[T](value: T, expected: String)(implicit reads: Reads[T], writes: Writes[T]) = {
    writes.writes(value) shouldBe Json.parse(expected)
    reads.reads(Json.parse(expected)) shouldBe JsSuccess(value)
  }

  test("sealed trait - case class child (wrapper object)") {
    import Default._
    roundTrip[Shape](Circle(1.0, "c"), """{"Circle":{"r":1.0,"label":"c"}}""")
  }

  test("sealed trait - case object child (wrapper object)") {
    import Default._
    roundTrip[Shape](Empty, """{"Empty":{}}""")
  }

  test("sealed trait inside containers") {
    import Default._
    roundTrip(
      Zoo(List(Dog("d"), Wolf(1, 2)), Some(Dog("f"))),
      """{"animals":[{"Dog":{"name":"d"}},{"Wolf":{"pack":1,"a":2}}],"favourite":{"Dog":{"name":"f"}}}"""
    )
  }

  test("single-field children are flat with CaseClass1ToValueClass") {
    import Flat._
    roundTrip[List[Mark]](List(Dot(2.0), Line(1, 2)), """[{"Dot":2.0},{"Line":{"a":1,"b":2}}]""")
  }

  test("nested sealed hierarchies are flattened to their leaves") {
    import Default._
    roundTrip[Animal](Dog("d"), """{"Dog":{"name":"d"}}""")
  }

  test("discriminator") {
    import Discriminated._
    roundTrip[Shape](Circle(1.0, "c"), """{"r":1.0,"label":"c","type":"Circle"}""")
    roundTrip[Shape](Empty, """{"type":"Empty"}""")
  }

  test("constructor naming") {
    import SnakeConstructors._
    roundTrip[Shape](Circle(1.0, "c"), """{"circle":{"r":1.0,"label":"c"}}""")
  }

  test("an unknown case lists the known ones") {
    import Default._
    val message = Json.parse("""{"Square":{}}""").validate[Shape] match {
      case JsError(errors) => errors.flatMap(_._2).flatMap(_.messages).mkString
      case other           => fail(s"expected an error, got $other")
    }
    message should include("Circle")
    message should include("Empty")
  }

  test("a case object at its own type is not derived (play's Writes is invariant, enum instances do not apply)") {
    import Enums._
    "Json.toJson(Color.DarkBlue)" shouldNot typeCheck
    """Json.obj("color" -> Color.DarkBlue)""" shouldNot typeCheck
    """Json.parse("{}").validate[Color.Red.type]""" shouldNot typeCheck
    "Json.toJson(Empty)" shouldNot typeCheck
  }

  test("enum instances still win over ADT derivation") {
    import Enums._
    Json.toJson[Color](Color.DarkBlue) shouldBe JsString("DarkBlue")
    Json.toJson(Paint(Color.DarkBlue, Level.High)) shouldBe Json.parse("""{"color":"DarkBlue","level":2}""")
    Json.toJson[Level](Level.High) shouldBe JsNumber(2)
  }
}
