package pl.iterators.kebs.scalacheck

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.scalacheck.model._

class AnyValGeneratorsTests extends AnyFunSuite with Matchers {

  object KebsProtocol extends KebsScalacheckGenerators

  object KebsProtocolWithFancyPredefs extends KebsScalacheckGenerators with KebsArbitraryPredefs

  test("Basic sample test") {
    import KebsProtocol._

    noException should be thrownBy allGenerators[BasicSample].normal.generate
  }
}
