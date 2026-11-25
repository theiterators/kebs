package pl.iterators.kebs.jsoniter.formats

import com.github.plokhotnyuk.jsoniter_scala.core.*
import pl.iterators.kebs.instances.TimeInstances
import pl.iterators.kebs.instances.time.mixins.InstantEpochMilliLong
import pl.iterators.kebs.jsoniter.KebsJsoniter
import pl.iterators.kebs.opaque.Opaque
import pl.iterators.kebs.testkit.nested.*

object JsoniterScala3TestDomain {
  object Protocol extends KebsJsoniter

  opaque type UserId = String
  object UserId extends Opaque[UserId, String]
  final case class User(id: UserId, others: Map[UserId, Int])

  trait Show[A] { def show(a: A): String }
  object Show   {
    inline given viaJson[A](using codec: JsonValueCodec[A]): Show[A] = new Show[A] {
      def show(a: A): String = writeToString(a)(using codec)
    }
  }
}

class JsoniterScala3Tests extends JsoniterTestBase {
  import JsoniterScala3TestDomain.*
  import Protocol.*

  test("opaque types") {
    roundTrip(UserId("x"), "\"x\"")
    roundTrip(User(UserId("x"), Map(UserId("y") -> 1)), """{"id":"x","others":{"y":1}}""")
  }

  test("inside an inline given") {
    import JsoniterNestedInstancesTestDomain.PageCodecs.*
    summon[Show[RecPage]].show(recPage) shouldBe """{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}"""
    summon[Show[Page[RecPage]]].show(Page(List(recPage))) shouldBe """{"DATA":[{"v":1,"kids":{"DATA":[{"v":2,"kids":{"DATA":[]}}]}}]}"""
  }

  test("an ambiguous type argument is reported, not derived around") {
    "implicitly[JsonValueCodec[JsoniterAmbiguityTestDomain.HasPair]]" shouldNot typeCheck
  }

  test("ambiguous converters are reported, not ignored") {
    object Ambiguous extends KebsJsoniter with TimeInstances with InstantEpochMilliLong
    "Ambiguous.deriveCodec[Int]" should compile
    "{ import Ambiguous.*; Ambiguous.deriveCodec[java.time.Instant] }" shouldNot typeCheck
    "{ import Ambiguous.*; Ambiguous.deriveCodec[pl.iterators.kebs.testkit.model.Event] }" shouldNot typeCheck
  }
}

object JsoniterAmbiguityTestDomain {
  import JsoniterNestedInstancesTestDomain.asString

  final case class Pair[A](c: Int, d: Int)
  object Pair {
    implicit def codec[A](implicit a: JsonValueCodec[A]): JsonValueCodec[Pair[A]] = asString(_ => Pair(0, 0), p => s"${p.c}/${p.d}")
  }
  final case class Ambiguous(n: Int)
  object Ambiguous {
    implicit val first: JsonValueCodec[Ambiguous]  = asString(s => Ambiguous(s.toInt), _.n.toString)
    implicit val second: JsonValueCodec[Ambiguous] = asString(s => Ambiguous(-s.toInt), a => (-a.n).toString)
  }
  final case class HasPair(p: Pair[Ambiguous], n: Int)
}
