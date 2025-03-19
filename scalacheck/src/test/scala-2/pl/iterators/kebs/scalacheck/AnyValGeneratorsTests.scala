package pl.iterators.kebs.scalacheck

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.scalacheck.model._

class AnyValGeneratorsTests extends AnyFunSuite with Matchers {

  test("Basic sample test") {
    import KebsArbitrarySupport._

    noException should be thrownBy generate[BasicSample]()
  }
}
