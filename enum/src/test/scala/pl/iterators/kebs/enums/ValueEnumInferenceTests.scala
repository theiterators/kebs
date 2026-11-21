package pl.iterators.kebs.enums

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.{ValueEnumLike, ValueEnumLikeEntry}

// the shape of every integration module's value enum codec: V is only known through ValueEnumLike
trait ValueCodec[A] { def describe: String }
object ValueCodec   {
  implicit val intCodec: ValueCodec[Int] = new ValueCodec[Int] { def describe = "int" }
  implicit def valueEnumCodec[V, E <: ValueEnumLikeEntry[V]](implicit ev: ValueEnumLike[V, E], c: ValueCodec[V]): ValueCodec[E] =
    new ValueCodec[E] { def describe = s"${c.describe}: ${ev.values.mkString(",")}" }
}

object ValueEnumInferenceTestDomain {
  sealed abstract class Size(val value: Int) extends ValueEnumLikeEntry[Int]
  case object S                              extends Size(1)
  case object L                              extends Size(3)
}

class ValueEnumInferenceTests extends AnyFunSuite with Matchers with KebsValueEnum {
  import ValueEnumInferenceTestDomain._

  test("value enum resolves when the value type is inferred") {
    implicitly[ValueCodec[Size]].describe shouldBe "int: S,L"
  }
}
