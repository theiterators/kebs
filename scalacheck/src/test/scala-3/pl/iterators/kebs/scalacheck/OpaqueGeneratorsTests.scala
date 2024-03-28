package pl.iterators.kebs.scalacheck

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.net.{URI, URL}
import java.time.{Duration, Instant, LocalDate, LocalDateTime, LocalTime, ZonedDateTime}
import pl.iterators.kebs.opaque.Opaque
import pl.iterators.kebs.scalacheck.model._

class OpaqueGeneratorsTests extends AnyFunSuite with Matchers {

  object KebsProtocol extends KebsScalacheckGenerators

  object KebsProtocolWithFancyPredefs extends KebsScalacheckGenerators with KebsArbitraryPredefs

  test("Basic sample with opaque type test") {
    import KebsProtocol._

    noException should be thrownBy allGenerators[BasicSampleWithOpaque].normal.generate
  }
}
