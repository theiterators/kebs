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

  object KebsProtocol extends KebsScalacheckGenerators

  object KebsProtocolWithFancyPredefs extends KebsScalacheckGenerators with KebsArbitraryPredefs

  test("Collections sample test") {
    import KebsProtocol._

    val minimal = allGenerators[
      CollectionsSample,
      DefaultGeneratorParametersProvider,
      DefaultGeneratorsMinimalArbitrarySupport,
      DefaultGeneratorsNormalArbitrarySupport,
      DefaultGeneratorsMaximalArbitrarySupport
    ].minimal.generate
    minimal.listOfNumbers shouldBe empty
    minimal.arrayOfNumbers shouldBe empty
    minimal.setOfNumbers shouldBe empty
    minimal.vectorOfNumbers shouldBe empty
    minimal.optionOfNumber shouldBe empty
    minimal.mapOfNumberString shouldBe empty

    val maximal = allGenerators[
      CollectionsSample,
      DefaultGeneratorParametersProvider,
      DefaultGeneratorsMinimalArbitrarySupport,
      DefaultGeneratorsNormalArbitrarySupport,
      DefaultGeneratorsMaximalArbitrarySupport
    ].maximal.generate
    maximal.listOfNumbers shouldNot be(empty)
    maximal.arrayOfNumbers shouldNot be(empty)
    maximal.setOfNumbers shouldNot be(empty)
    maximal.vectorOfNumbers shouldNot be(empty)
    maximal.optionOfNumber shouldNot be(empty)
    maximal.mapOfNumberString shouldNot be(empty)
  }
  test("Non standard types sample test") {
    import KebsProtocolWithFancyPredefs._

    noException should be thrownBy allGenerators[
      NonStandardTypesSample,
      DefaultGeneratorParametersProvider,
      DefaultGeneratorsMinimalArbitrarySupport,
      DefaultGeneratorsNormalArbitrarySupport,
      DefaultGeneratorsMaximalArbitrarySupport
    ].normal.generate
  }
}
