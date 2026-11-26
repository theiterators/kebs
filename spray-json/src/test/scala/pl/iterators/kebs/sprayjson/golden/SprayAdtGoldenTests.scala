package pl.iterators.kebs.sprayjson.golden

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.sprayjson.{KebsSprayJson, KebsSprayJsonEnums, KebsSprayJsonValueEnums}
import pl.iterators.kebs.testkit.model._
import spray.json._

object SprayAdtGoldenTestDomain {
  sealed trait Mark
  final case class Dot(size: Double)    extends Mark
  final case class Line(a: Int, b: Int) extends Mark
  sealed trait Animal
  sealed trait Pet                         extends Animal
  final case class Dog(name: String)       extends Pet
  final case class Wolf(pack: Int, a: Int) extends Animal
  final case class Zoo(animals: List[Animal], favourite: Option[Animal])
}

// same models and JSON as CirceAdtGoldenTests: every JSON module writes ADTs the same way
class SprayAdtGoldenTests extends AnyFunSuite with Matchers {
  import SprayAdtGoldenTestDomain._

  object Default       extends DefaultJsonProtocol with KebsSprayJson
  object Flat          extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass
  object Discriminated extends DefaultJsonProtocol with KebsSprayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withDiscriminator("type")
  }
  object SnakeConstructors extends DefaultJsonProtocol with KebsSprayJson {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withConstructorNaming(NamingStyle.snakeCase)
  }
  object Enums
      extends DefaultJsonProtocol
      with KebsSprayJson
      with KebsEnumeratum
      with KebsValueEnumeratum
      with KebsSprayJsonEnums
      with KebsSprayJsonValueEnums

  private def roundTrip[T](value: T, expected: String)(implicit format: JsonFormat[T]) = {
    format.write(value) shouldBe expected.parseJson
    format.read(expected.parseJson) shouldBe value
  }

  test("sealed trait - case class child (wrapper object)") {
    import Default._
    roundTrip[Shape](Circle(1.0, "c"), """{"Circle":{"r":1.0,"label":"c"}}""")
  }

  test("sealed trait - case object child (wrapper object)") {
    import Default._
    roundTrip[Shape](Empty, """{"Empty":{}}""")
  }

  test("sealed trait as a root format and inside containers") {
    import Default._
    implicitly[RootJsonFormat[Shape]].write(Empty) shouldBe """{"Empty":{}}""".parseJson
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
    val error = intercept[DeserializationException](implicitly[JsonFormat[Shape]].read("""{"Square":{}}""".parseJson))
    error.msg should include("Circle")
    error.msg should include("Empty")
  }

  test("enum formats still win over ADT derivation") {
    import Enums._
    implicitly[JsonFormat[Color]].write(Color.DarkBlue) shouldBe JsString("DarkBlue")
    implicitly[RootJsonFormat[Paint]].write(Paint(Color.DarkBlue, Level.High)) shouldBe """{"color":"DarkBlue","level":2}""".parseJson
    implicitly[JsonFormat[Level]].write(Level.High) shouldBe JsNumber(2)
  }
}
