package pl.iterators.kebs.sprayjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.sprayjson.KebsSprayJson
import pl.iterators.kebs.testkit.large._
import pl.iterators.kebs.testkit.nested._
import spray.json._

object SprayUsageTestDomain {
  object Protocol extends DefaultJsonProtocol with KebsSprayJson
  object Flat     extends DefaultJsonProtocol with KebsSprayJson with CaseClass1ToValueClass

  // a format derived in the arguments of a super constructor
  abstract class Repository[A](val name: String)(implicit val format: JsonFormat[A])
  import Protocol._
  object RecListRepository        extends Repository[RecList]("recList")(jsonFormatN[RecList])
  object HasListRepository        extends Repository[HasList]("hasList")
  class AdtRepository(val n: Int) extends Repository[HasAdt]("hasAdt") {
    def this(json: String) = this(json.parseJson.convertTo[HasList].n)
  }

  final case class Generic[A](x: A, n: Int)

  sealed trait Special
  case object `my-case`                     extends Special
  final case class `other+case`(`f-f`: Int) extends Special

  final case class Hidden private (a: Int, b: Int = 3)
  object Hidden { def of(a: Int, b: Int): Hidden = new Hidden(a, b) }

  final case class Money(amount: Int, currency: String)
  final case class HasMoney(money: Money, n: Int)
  object MoneyProtocol extends DefaultJsonProtocol with KebsSprayJson {
    implicit val moneyConverter: InstanceConverter[Money, String] = InstanceConverter[Money, String](
      money => s"${money.amount} ${money.currency}",
      text => {
        val Array(amount, currency) = text.split(' ')
        Money(amount.toInt, currency)
      }
    )
  }

  object EarlyProtocol extends DefaultJsonProtocol with KebsSprayJson {
    val early: RootJsonFormat[HasList]      = jsonFormatN[HasList]
    override val kebsJsonConfig: JsonConfig = JsonConfig.default
  }

  final case class Overloaded(value: String, n: Int)
  object Overloaded {
    def apply(value: CharSequence, n: Int): Overloaded = new Overloaded(s"overload:$value", n)
  }

  sealed trait Typed
  final case class Named(`type`: String) extends Typed
  object TypeProtocol                    extends DefaultJsonProtocol with KebsSprayJson {
    override def kebsJsonConfig: JsonConfig = JsonConfig.default.withDiscriminator("type")
  }
}

class SprayUsageTests extends AnyFunSuite with Matchers {
  import SprayUsageTestDomain._

  test("in the arguments of super constructors and auxiliary constructors") {
    RecListRepository.format.write(RecList(1, List(RecList(2, Nil)))).compactPrint shouldBe """{"kids":[{"kids":[],"v":2}],"v":1}"""
    HasListRepository.format.write(HasList(List(item), 1)).compactPrint shouldBe """{"n":1,"x":[{"n":1,"name":"i"}]}"""
    new AdtRepository("""{"n":7,"x":[]}""").n shouldBe 7
  }

  test("large sealed hierarchies") {
    import Protocol._
    val format = implicitly[RootJsonFormat[Large]]
    List[Large](C1(1, X1(2)), C150(3, X150(4)), O120).foreach(large => format.read(format.write(large)) shouldBe large)
  }

  test("an Option that is only known as a type parameter") {
    import Protocol._
    def genericFormat[A: JsonFormat]: RootJsonFormat[Generic[A]] = jsonFormatN[Generic[A]]
    val format                                                   = genericFormat[Option[Int]]
    format.write(Generic(None, 1)).compactPrint shouldBe """{"n":1}"""
    format.write(Generic(Some(2), 1)).compactPrint shouldBe """{"n":1,"x":2}"""
    format.read("""{"n":1}""".parseJson) shouldBe Generic(None, 1)
    format.read("""{"n":1,"x":null}""".parseJson) shouldBe Generic(None, 1)
  }

  test("an implicit def that derives a generic type") {
    import Protocol._
    object Formats { implicit def generic[A: JsonFormat]: RootJsonFormat[Generic[A]] = jsonFormatN[Generic[A]] }
    import Formats._
    implicitly[RootJsonFormat[Generic[Item]]].write(Generic(item, 1)).compactPrint shouldBe """{"n":1,"x":{"n":1,"name":"i"}}"""
  }

  test("case names that are not plain identifiers") {
    import Protocol._
    val format = implicitly[RootJsonFormat[Special]]
    format.write(`my-case`).compactPrint shouldBe """{"my-case":{}}"""
    format.write(`other+case`(1)).compactPrint shouldBe """{"other+case":{"f-f":1}}"""
    format.read("""{"my-case":{}}""".parseJson) shouldBe `my-case`
  }

  test("a private constructor with default values") {
    import Protocol._
    val format = implicitly[RootJsonFormat[Hidden]]
    format.write(Hidden.of(1, 2)).compactPrint shouldBe """{"a":1,"b":2}"""
    format.read("""{"a":1,"b":2}""".parseJson) shouldBe Hidden.of(1, 2)
  }

  test("a case class with an InstanceConverter") {
    import MoneyProtocol._
    val money = Money(1, "x")
    implicitly[JsonFormat[Money]].write(money) shouldBe JsString("1 x")
    implicitly[JsonFormat[List[Money]]].write(List(money)).compactPrint shouldBe """["1 x"]"""
    implicitly[RootJsonFormat[HasMoney]].write(HasMoney(money, 1)).compactPrint shouldBe """{"money":"1 x","n":1}"""
  }

  test("a config that is initialised after the format") {
    EarlyProtocol.early.write(HasList(List(item), 1)).compactPrint shouldBe """{"n":1,"x":[{"n":1,"name":"i"}]}"""
  }

  test("the companion apply that matches the fields exactly") {
    import Protocol._
    implicitly[RootJsonFormat[Overloaded]].read("""{"value":"a","n":1}""".parseJson).value shouldBe "a"
  }

  test("a field with the name of the discriminator") {
    import TypeProtocol._
    the[SerializationException] thrownBy implicitly[RootJsonFormat[Typed]].write(Named("n")) should have message
    "Named has a field 'type' of its own, so it cannot carry the 'type' discriminator"
  }
}
