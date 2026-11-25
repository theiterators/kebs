package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.testkit.nested._

object JsoniterUsageTestDomain {
  object Protocol extends KebsJsoniter
  import Protocol._

  // codecs derived in the arguments of a super constructor
  abstract class Repository[A](val name: String)(implicit val codec: JsonValueCodec[A])
  object HasListRepository        extends Repository[HasList]("hasList")
  object RecListRepository        extends Repository[RecList]("recList")
  class GenRepository(val n: Int) extends Repository[Gen[Item]]("gen") {
    def this(json: String) = this(readFromString[HasList](json).n)
  }

  final case class Hidden private (a: Int, b: Int = 3)
  object Hidden { def of(a: Int, b: Int): Hidden = new Hidden(a, b) }

  object EarlyProtocol extends KebsJsoniter {
    val early: JsonValueCodec[HasList]      = implicitly[JsonValueCodec[HasList]]
    override val kebsJsonConfig: JsonConfig = JsonConfig.default
  }

  final case class Tag[A](v: Int)
  final case class HasTag(tag: Tag[java.io.InputStream], n: Int)

  type Name       = String
  type Names      = List[Name]
  type Maybe[A]   = Option[A]
  type Pair       = (Int, Name)
  type Dict[V]    = Map[Name, V]
  type AliasedRec = RecList
  final case class Aliases(name: Name, names: Names, maybe: Maybe[Name], pair: Pair, dict: Dict[Names], rec: AliasedRec)

  final case class Shadowing(in: Int, out: Int, obj: Int, values: Int, fields: Int, config: Int, codecs: Int, T: Int)
  final case class ManyTypes[A, B](a: A, b: B, either: List[(A, B)], nested: Option[Map[String, ManyTypes[B, A]]])

  class Outer {
    final case class Inner(a: Int, b: Int)
    def inner: JsonValueCodec[Inner] = implicitly[JsonValueCodec[Inner]]
  }

  class ProtocolInClass(config: JsonConfig) extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = config
    def write(list: RecList): String        = writeToString(list)
  }
}

class JsoniterUsageTests extends JsoniterTestBase {
  import JsoniterUsageTestDomain._

  test("in the arguments of super constructors and auxiliary constructors") {
    roundTrip(RecList(1, List(RecList(2, Nil))), """{"v":1,"kids":[{"v":2,"kids":[]}]}""")(RecListRepository.codec)
    roundTrip(HasList(List(item), 1), """{"x":[{"name":"i","n":1}],"n":1}""")(HasListRepository.codec)
    new GenRepository("""{"x":[],"n":7}""").n shouldBe 7
  }

  test("a private constructor with default values") {
    import Protocol._
    roundTrip(Hidden.of(1, 2), """{"a":1,"b":2}""")
  }

  test("a config that is initialised after the codec") {
    roundTrip(HasList(List(item), 1), """{"x":[{"name":"i","n":1}],"n":1}""")(EarlyProtocol.early)
  }

  test("a protocol that is a class") {
    import pl.iterators.kebs.core.naming.{Naming => NamingStyle}
    new ProtocolInClass(JsonConfig.default).write(RecList(1, Nil)) shouldBe """{"v":1,"kids":[]}"""
    new ProtocolInClass(JsonConfig.default.withFieldNaming(NamingStyle.uppercase)).write(RecList(1, Nil)) shouldBe """{"V":1,"KIDS":[]}"""
  }

  test("a type argument that a derived type does not use") {
    import Protocol._
    roundTrip(HasTag(Tag(1), 2), """{"tag":{"v":1},"n":2}""")
  }

  test("type aliases") {
    import Protocol._
    roundTrip(
      Aliases("n", List("a"), Some("m"), (1, "p"), Map("k" -> List("v")), RecList(1, Nil)),
      """{"name":"n","names":["a"],"maybe":"m","pair":[1,"p"],"dict":{"k":["v"]},"rec":{"v":1,"kids":[]}}"""
    )
    roundTrip[Names](List("a"), """["a"]""")
    roundTrip[AliasedRec](RecList(1, Nil), """{"v":1,"kids":[]}""")
  }

  test("field names that generated code uses too") {
    import Protocol._
    roundTrip(
      Shadowing(1, 2, 3, 4, 5, 6, 7, 8),
      """{"in":1,"out":2,"obj":3,"values":4,"fields":5,"config":6,"codecs":7,"T":8}"""
    )
  }

  test("several type parameters, swapped in the recursion") {
    import Protocol._
    roundTrip(
      ManyTypes(1, "b", List((2, "c")), Some(Map("k" -> ManyTypes("x", 3, Nil, None)))),
      """{"a":1,"b":"b","either":[[2,"c"]],"nested":{"k":{"a":"x","b":3,"either":[],"nested":null}}}"""
    )
  }

  test("classes inside classes and methods") {
    import Protocol._
    val outer = new Outer
    roundTrip(outer.Inner(1, 2), """{"a":1,"b":2}""")(outer.inner)

    final case class Local(a: Int, b: List[String])
    roundTrip(Local(1, List("x")), """{"a":1,"b":["x"]}""")
    roundTrip(List(Local(1, Nil)), """[{"a":1,"b":[]}]""")
  }
}
