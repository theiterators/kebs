package pl.iterators.kebs.playjson.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.playjson.KebsPlayJson
import pl.iterators.kebs.testkit.large._
import pl.iterators.kebs.testkit.nested._
import play.api.libs.json._

object PlayUsageTestDomain {
  object Protocol extends KebsPlayJson
  import Protocol._

  // instances derived in the arguments of a super constructor
  abstract class Repository[A](val name: String)(implicit val reads: Reads[A], val writes: Writes[A])
  object HasListRepository        extends Repository[HasList]("hasList")
  object RecListRepository        extends Repository[RecList]("recList")(deriveReads[RecList], deriveWrites[RecList])
  class AdtRepository(val n: Int) extends Repository[HasAdt]("hasAdt") {
    def this(json: String) = this(Json.parse(json).as[HasList].n)
  }

  // cases that also occur on their own
  sealed trait Sum
  final case class Own(n: Int) extends Sum
  object Own { implicit val writes: OWrites[Own] = OWrites(own => Json.obj("OWN" -> own.n)) }
  final case class Derived(m: Int)                         extends Sum
  final case class Several(l: List[Derived], o: List[Own]) extends Sum
  final case class HasCases(
      s: Sum,
      l: List[Own],
      v: Vector[Own],
      set: Set[Own],
      m: Map[String, List[Own]],
      o: Option[Seq[Own]],
      d: List[Derived],
      g: Gen[List[Own]],
      b: PlayNestedInstancesTestDomain.Box[List[Own]]
  )

  sealed trait Special
  case object `my-case`                     extends Special
  final case class `other+case`(`f-f`: Int) extends Special

  final case class Hidden private (a: Int, b: Int = 3)
  object Hidden { def of(a: Int, b: Int): Hidden = new Hidden(a, b) }

  object EarlyProtocol extends KebsPlayJson {
    val early: OFormat[HasList]             = deriveFormat[HasList]
    override val kebsJsonConfig: JsonConfig = JsonConfig.default
  }

  import PlayNestedInstancesTestDomain.{Both, RecBoth}
  object DefinedBoth {
    implicit val format: OFormat[RecBoth] = deriveFormat[RecBoth]
  }
  val both: RecBoth = RecBoth(1, Some(Both(RecBoth(2, None))))

  sealed trait Typed
  final case class Named(`type`: String) extends Typed
  object TypeProtocol                    extends KebsPlayJson {
    override def kebsJsonConfig: JsonConfig = JsonConfig.default.withDiscriminator("type")
  }

  final case class Rose[A](v: A, kids: List[Rose[A]])
  object OneDirection {
    implicit def roseReads[A: Reads]: Reads[Rose[A]]     = deriveReads[Rose[A]]
    implicit def roseWrites[A: Writes]: OWrites[Rose[A]] = deriveWrites[Rose[A]]
  }
  object BothDirections {
    implicit def roseFormat[A: Format]: OFormat[Rose[A]] = deriveFormat[Rose[A]]
  }
}

class PlayUsageTests extends AnyFunSuite with Matchers {
  import PlayUsageTestDomain._

  private def written[A](a: A)(implicit writes: Writes[A]) = Json.stringify(writes.writes(a))

  test("in the arguments of super constructors and auxiliary constructors") {
    written(RecList(1, List(RecList(2, Nil))))(RecListRepository.writes) shouldBe """{"v":1,"kids":[{"v":2,"kids":[]}]}"""
    written(HasList(List(item), 1))(HasListRepository.writes) shouldBe """{"x":[{"name":"i","n":1}],"n":1}"""
    new AdtRepository("""{"n":7,"x":[]}""").n shouldBe 7
  }

  test("large sealed hierarchies") {
    import Protocol._
    val format = deriveFormat[Large]
    List[Large](C1(1, X1(2)), C150(3, X150(4)), O120).foreach(large => format.reads(format.writes(large)) shouldBe JsSuccess(large))
  }

  test("collections of the cases of a sealed hierarchy") {
    import Protocol._
    val own = Own(1)
    written(
      HasCases(
        own,
        List(own),
        Vector(own),
        Set(own),
        Map("k" -> List(own)),
        Some(Seq(own)),
        List(Derived(2)),
        Gen(List(own), 1),
        PlayNestedInstancesTestDomain.Box(List(List(own)))
      )
    ) shouldBe
    """{"s":{"Own":{"OWN":1}},"l":[{"OWN":1}],"v":[{"OWN":1}],"set":[{"OWN":1}],"m":{"k":[{"OWN":1}]},"o":[{"OWN":1}],""" +
    """"d":[{"m":2}],"g":{"x":[{"OWN":1}],"n":1},"b":{"DATA":[[{"OWN":1}]]}}"""
    written[Sum](Several(List(Derived(1)), List(own))) shouldBe """{"Several":{"l":[{"m":1}],"o":[{"OWN":1}]}}"""
  }

  test("case names that are not plain identifiers") {
    import Protocol._
    written[Special](`my-case`) shouldBe """{"my-case":{}}"""
    written[Special](`other+case`(1)) shouldBe """{"other+case":{"f-f":1}}"""
    Json.parse("""{"my-case":{}}""").as[Special] shouldBe `my-case`
  }

  test("a private constructor with default values") {
    import Protocol._
    written(Hidden.of(1, 2)) shouldBe """{"a":1,"b":2}"""
    Json.parse("""{"a":1,"b":2}""").as[Hidden] shouldBe Hidden.of(1, 2)
  }

  test("a config that is initialised after the format") {
    written(HasList(List(item), 1))(EarlyProtocol.early) shouldBe """{"x":[{"name":"i","n":1}],"n":1}"""
  }

  test("an implicit Format of a recursive type that needs both directions") {
    import DefinedBoth._
    written(both) shouldBe """{"v":1,"next":{"BOTH":{"v":2}}}"""
    Json.parse("""{"v":1,"next":{"BOTH":{"v":2}}}""").as[PlayNestedInstancesTestDomain.RecBoth] shouldBe both
  }

  test("a field with the name of the discriminator") {
    import TypeProtocol._
    the[IllegalArgumentException] thrownBy implicitly[Writes[Typed]].writes(Named("n")) should have message
    "Named has a field 'type' of its own, so it cannot carry the 'type' discriminator"
  }

  test("implicit defs that derive a generic recursive type, in one direction or both") {
    import Protocol._
    val rose = Rose(item, List(Rose(item, Nil)))
    val json = """{"v":{"name":"i","n":1},"kids":[{"v":{"name":"i","n":1},"kids":[]}]}"""
    locally {
      import OneDirection._
      written(rose) shouldBe json
      Json.parse(json).as[Rose[Item]] shouldBe rose
    }
    locally {
      import BothDirections._
      written(rose) shouldBe json
      Json.parse(json).as[Rose[Item]] shouldBe rose
    }
  }
}
