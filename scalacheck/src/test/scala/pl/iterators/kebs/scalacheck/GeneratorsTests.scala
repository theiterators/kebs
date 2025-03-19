package pl.iterators.kebs.scalacheck

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.{URI, URL}
import java.time._

case class CollectionsSample(
    listOfNumbers: List[Int],
    arrayOfNumbers: Array[Int],
    setOfNumbers: Set[Int],
    vectorOfNumbers: Vector[Int],
    optionOfNumber: Option[Int],
    mapOfNumberString: Map[Int, String]
)

case class NonStandardTypesSample(
    instant: Instant,
    zonedDateTime: ZonedDateTime,
    localDateTime: LocalDateTime,
    localDate: LocalDate,
    localTime: LocalTime,
    duration: Duration,
    url: URL,
    uri: URI
)

class GeneratorsTests extends AnyFunSuite with Matchers {
  import KebsArbitrarySupport._

  test("Collections sample test") {
    noException should be thrownBy generate[CollectionsSample]()
  }
  test("Non standard types sample test") {
    noException should be thrownBy generate[NonStandardTypesSample]()
  }

}
