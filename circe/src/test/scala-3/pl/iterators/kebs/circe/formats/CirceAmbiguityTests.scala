package pl.iterators.kebs.circe.formats

import io.circe.Encoder
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce

object CirceAmbiguityTestDomain {
  object Protocol extends KebsCirce

  // the argument is ambiguous, so there is no codec of the generic type either
  final case class Pair[A](c: Int, d: Int)
  object Pair { implicit def encoder[A](implicit a: Encoder[A]): Encoder[Pair[A]] = Encoder.encodeString.contramap(p => s"${p.c}/${p.d}") }
  final case class Ambiguous(n: Int)
  object Ambiguous {
    implicit val first: Encoder[Ambiguous]  = Encoder.encodeInt.contramap(_.n)
    implicit val second: Encoder[Ambiguous] = Encoder.encodeInt.contramap(-_.n)
  }
  final case class HasPair(p: Pair[Ambiguous], n: Int)
}

// Scala 2 reports an ambiguity that a macro meets even where its failure is expected
class CirceAmbiguityTests extends AnyFunSuite with Matchers {
  import CirceAmbiguityTestDomain._

  test("an ambiguous type argument is reported, not derived around") {
    import Protocol._
    "Encoder[HasPair]" shouldNot typeCheck
  }
}
