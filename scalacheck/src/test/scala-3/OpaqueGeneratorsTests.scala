package pl.iterators.kebs.scalacheck

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.net.{URI, URL}
import java.time.{Duration, Instant, LocalDate, LocalDateTime, LocalTime, ZonedDateTime}
import pl.iterators.kebs.opaque.Opaque


case class WrappedInt(int: Int)

opaque type OpaqueInt = Int
object OpaqueInt extends Opaque[OpaqueInt, Int] {
  override def apply(value: Int) = value
}

case class BasicSampleWithOpaque(
    someNumber: Int,
    someText: String,
    wrappedNumber: WrappedInt,
    opaqueInt: OpaqueInt
)

class OpaqueGeneratorsTests extends AnyFunSuite with Matchers {

  object KebsProtocol extends KebsScalacheckGenerators

  object KebsProtocolWithFancyPredefs extends KebsScalacheckGenerators with KebsArbitraryPredefs

  test("Basic sample with opaque type test") {
    import KebsProtocol._

    noException should be thrownBy allGenerators[BasicSampleWithOpaque].normal.generate
  }
}
