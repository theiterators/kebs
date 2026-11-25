package pl.iterators.kebs.jsoniter.golden

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.jsoniter.enums.{KebsJsoniterEnums, KebsJsoniterValueEnums}
import pl.iterators.kebs.jsoniter.formats.JsoniterTestBase
import pl.iterators.kebs.testkit.large._
import pl.iterators.kebs.testkit.model._
import pl.iterators.kebs.testkit.{nested => n}

object JsoniterAdtGoldenTestDomain {
  sealed trait Mark
  final case class Dot(size: Double)    extends Mark
  final case class Line(a: Int, b: Int) extends Mark
  final case class Marks(marks: List[Mark])
  sealed trait Animal
  sealed trait Pet                         extends Animal
  final case class Dog(name: String)       extends Pet
  final case class Wolf(pack: Int, a: Int) extends Animal
  final case class Zoo(animals: List[Animal], favourite: Option[Animal])
  sealed trait Special
  case object `my-case`                     extends Special
  final case class `other+case`(`f-f`: Int) extends Special
  sealed trait Both
  sealed trait Left            extends Both
  sealed trait Right           extends Both
  final case class Mid(n: Int) extends Left with Right
  sealed trait Box[+A]
  final case class Full[A](a: A) extends Box[A]
  case object Hollow             extends Box[Nothing]
}

// the models and JSON of the circe, spray-json and play-json ADT goldens
class JsoniterAdtGoldenTests extends JsoniterTestBase {
  import JsoniterAdtGoldenTestDomain._

  object Default           extends KebsJsoniter
  object Flat              extends KebsJsoniter with CaseClass1ToValueClass
  object SnakeConstructors extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withConstructorNaming(NamingStyle.snakeCase)
  }
  object Enums extends KebsJsoniter with KebsEnumeratum with KebsValueEnumeratum with KebsJsoniterEnums with KebsJsoniterValueEnums

  test("case class child (wrapper object)") {
    import Default._
    roundTrip[Shape](Circle(1.0, "c"), """{"Circle":{"r":1.0,"label":"c"}}""")
  }

  test("case object child (wrapper object)") {
    import Default._
    roundTrip[Shape](Empty, """{"Empty":{}}""")
    readFromString[Shape]("""{"Empty":{"a":[1]}}""") shouldBe Empty
  }

  test("inside containers") {
    import Default._
    roundTrip(
      Zoo(List(Dog("d"), Wolf(1, 2)), Some(Dog("f"))),
      """{"animals":[{"Dog":{"name":"d"}},{"Wolf":{"pack":1,"a":2}}],"favourite":{"Dog":{"name":"f"}}}"""
    )
    roundTrip[List[Shape]](List(Empty), """[{"Empty":{}}]""")
  }

  test("a child at its own type is a case class") {
    import Default._
    roundTrip(Circle(1.0, "c"), """{"r":1.0,"label":"c"}""")
  }

  test("single-field children are flat with CaseClass1ToValueClass") {
    import Flat._
    roundTrip[List[Mark]](List(Dot(2.0), Line(1, 2)), """[{"Dot":2.0},{"Line":{"a":1,"b":2}}]""")
    roundTrip(Marks(List(Dot(2.0))), """[{"Dot":2.0}]""")
  }

  test("nested sealed hierarchies are flattened to their leaves") {
    import Default._
    roundTrip[Animal](Dog("d"), """{"Dog":{"name":"d"}}""")
    roundTrip[Pet](Dog("d"), """{"Dog":{"name":"d"}}""")
    roundTrip[Both](Mid(1), """{"Mid":{"n":1}}""")
  }

  test("generic hierarchies") {
    import Default._
    roundTrip[Box[Int]](Full(1), """{"Full":{"a":1}}""")
    roundTrip[Box[List[Shape]]](Full(List(Empty)), """{"Full":{"a":[{"Empty":{}}]}}""")
    roundTrip[Box[Int]](Hollow, """{"Hollow":{}}""")
  }

  test("constructor naming") {
    import SnakeConstructors._
    roundTrip[Shape](Circle(1.0, "c"), """{"circle":{"r":1.0,"label":"c"}}""")
  }

  test("case names that are not plain identifiers") {
    import Default._
    roundTrip[Special](`my-case`, """{"my-case":{}}""")
    roundTrip[Special](`other+case`(1), """{"other+case":{"f-f":1}}""")
  }

  test("enum codecs win over the derivation of a hierarchy") {
    import Enums._
    writeToString[Color](Color.DarkBlue) shouldBe "\"DarkBlue\""
    roundTrip(Paint(Color.DarkBlue, Level.High), """{"color":"DarkBlue","level":2}""")
  }

  test("recursive hierarchies") {
    import Default._
    roundTrip(
      n.adt,
      """{"Branch":{"l":{"items":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}}"""
    )
  }

  test("a user's generic codec inside a recursive hierarchy") {
    import Default._, pl.iterators.kebs.jsoniter.formats.JsoniterNestedInstancesTestDomain.PageCodecs._
    roundTrip(n.adt, """{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}}""")
    roundTrip(
      n.HasAdt(n.adt, 1),
      """{"x":{"Branch":{"l":{"DATA":[{"Leaf":{"n":1,"i":{"name":"i","n":1}}},{"Empty":{}}]},"r":[{"Empty":{}}]}},"n":1}"""
    )
  }

  test("large hierarchies") {
    import Default._
    val codec = implicitly[JsonValueCodec[Large]]
    List[Large](C1(1, X1(2)), C150(3, X150(4)), O120).foreach { large =>
      readFromString[Large](writeToString(large)(codec))(codec) shouldBe large
    }
  }

  test("what is not a case") {
    import Default._
    rejected[Shape]("{}", "expected one of: Circle, Empty")
    rejected[Shape]("""{"Square":{}}""", "unknown case 'Square', expected one of: Circle, Empty")
    rejected[Shape]("\"Empty\"", "Circle, Empty")
    rejected[Shape]("null", "")
    rejected[Shape]("""{"Empty":{},"Circle":{"r":1.0,"label":"c"}}""", "")
    rejected[Shape]("""{"Empty":null}""", "")
    rejected[Shape]("""{"Circle":{"r":1.0}}""", "\"label\"")
    (0 until 10).foreach(length => rejected[Shape]("""{"Empty":{}""".take(length), ""))
  }
}
