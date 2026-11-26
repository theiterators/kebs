package pl.iterators.kebs.circe.formats

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.testkit.large._
import pl.iterators.kebs.testkit.nested._

object CirceUsageTestDomain {
  object Protocol extends KebsCirce
  object Flat     extends KebsCirce with CaseClass1ToValueClass
  import Protocol._

  // codecs derived in the arguments of a super constructor
  abstract class Repository[A](val name: String)(implicit val encoder: Encoder[A], val decoder: Decoder[A])
  object HasListRepository        extends Repository[HasList]("hasList")
  object RecListRepository        extends Repository[RecList]("recList")
  class AdtRepository(val n: Int) extends Repository[HasAdt]("hasAdt") {
    def this(json: Json) = this(json.as[HasList].fold(throw _, _.n))
  }

  class Suite {
    object Domain {
      final case class Plain(a: Int, b: Int = 3)
      sealed trait Sum
      final case class Case(a: Int, b: Plain) extends Sum
      case object Object                      extends Sum
    }
    val encoder: Encoder[Domain.Sum] = Encoder[Domain.Sum]
    val decoder: Decoder[Domain.Sum] = Decoder[Domain.Sum]
  }

  sealed trait Special
  case object `my-case`                     extends Special
  final case class `other+case`(`f-f`: Int) extends Special

  final case class Hidden private (a: Int, b: Int = 3)
  object Hidden { def of(a: Int, b: Int): Hidden = new Hidden(a, b) }

  object EarlyProtocol extends KebsCirce {
    val early: Encoder[HasList]             = Encoder[HasList]
    override val kebsJsonConfig: JsonConfig = JsonConfig.default
  }

  final case class Counted(v: Int, next: Option[Counted])
  object Counted { implicit val encoder: Encoder[Counted] = Encoder.encodeInt.contramap(_.v) }

  sealed trait Typed
  final case class Named(`type`: String) extends Typed
  object TypeProtocol                    extends KebsCirce {
    override def kebsJsonConfig: JsonConfig = JsonConfig.default.withDiscriminator("type")
  }

  final case class Ambiguous(n: Int)
  object Ambiguous {
    implicit val first: Encoder[Ambiguous]  = Encoder.encodeInt.contramap(_.n)
    implicit val second: Encoder[Ambiguous] = Encoder.encodeInt.contramap(-_.n)
  }
  final case class Tag[A](v: Int)
  final case class HasTag(tag: Tag[Ambiguous], n: Int)
}

class CirceUsageTests extends AnyFunSuite with Matchers {
  import CirceUsageTestDomain._

  test("in the arguments of super constructors and auxiliary constructors") {
    RecListRepository.encoder(RecList(1, List(RecList(2, Nil)))).noSpaces shouldBe """{"v":1,"kids":[{"v":2,"kids":[]}]}"""
    HasListRepository.encoder(HasList(List(item), 1)).noSpaces shouldBe """{"x":[{"name":"i","n":1}],"n":1}"""
    new AdtRepository(Json.obj("x" -> Json.arr(), "n" -> Json.fromInt(7))).n shouldBe 7
  }

  test("large sealed hierarchies") {
    import Protocol._
    val encoder = Encoder[Large]
    val decoder = Decoder[Large]
    List[Large](C1(1, X1(2)), C150(3, X150(4)), O120).foreach(large => decoder.decodeJson(encoder(large)) shouldBe Right(large))
  }

  test("an Encoder.AsObject of a type that has an Encoder: nested uses keep it") {
    import Protocol._
    Encoder.AsObject[Counted].apply(Counted(1, Some(Counted(2, None)))).noSpaces shouldBe """{"v":1,"next":2}"""
  }

  test("an Encoder.AsObject of a flat type: nested uses stay flat") {
    import Flat._
    val tree = FlatChildren(List(FlatTree(1, FlatChildren(Nil))))
    Encoder.AsObject[FlatChildren].apply(tree).noSpaces shouldBe """{"list":[{"value":1,"children":[]}]}"""
  }

  test("case names that are not plain identifiers") {
    import Protocol._
    Encoder[Special].apply(`my-case`).noSpaces shouldBe """{"my-case":{}}"""
    Encoder[Special].apply(`other+case`(1)).noSpaces shouldBe """{"other+case":{"f-f":1}}"""
    io.circe.parser.decode[Special]("""{"my-case":{}}""") shouldBe Right(`my-case`)
  }

  test("a private constructor with default values") {
    import Protocol._
    Encoder[Hidden].apply(Hidden.of(1, 2)).noSpaces shouldBe """{"a":1,"b":2}"""
    io.circe.parser.decode[Hidden]("""{"a":1,"b":2}""") shouldBe Right(Hidden.of(1, 2))
  }

  test("a config that is initialised after the codec") {
    EarlyProtocol.early(HasList(List(item), 1)).noSpaces shouldBe """{"x":[{"name":"i","n":1}],"n":1}"""
  }

  test("a field with the name of the discriminator") {
    import TypeProtocol._
    the[IllegalArgumentException] thrownBy Encoder[Typed].apply(Named("n")) should have message
    "Named has a field 'type' of its own, so it cannot carry the 'type' discriminator"
  }

  test("a type argument that a derived type does not use may be ambiguous") {
    import Protocol._
    Encoder[HasTag].apply(HasTag(Tag(1), 2)).noSpaces shouldBe """{"tag":{"v":1},"n":2}"""
  }

  test("classes and objects in an object inside a class") {
    val suite = new Suite
    import suite.Domain._
    suite.encoder(Case(1, Plain(2, 3))).noSpaces shouldBe """{"Case":{"a":1,"b":{"a":2,"b":3}}}"""
    suite.decoder.decodeJson(suite.encoder(Case(1, Plain(2, 3)))) shouldBe Right(Case(1, Plain(2, 3)))
    suite.decoder.decodeJson(Json.obj("Object" -> Json.obj())) shouldBe Right(Object)
  }
}
