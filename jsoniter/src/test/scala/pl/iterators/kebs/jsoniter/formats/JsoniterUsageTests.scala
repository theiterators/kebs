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
}
