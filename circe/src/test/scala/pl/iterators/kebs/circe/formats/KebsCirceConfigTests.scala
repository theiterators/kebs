package pl.iterators.kebs.circe.formats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.internal.KebsCirceConfig
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.naming.Naming

class KebsCirceConfigTests extends AnyFunSuite with Matchers {
  test("kindlings gets memoised namings") {
    var calls  = 0
    val config = JsonConfig.default.withFieldNaming(Naming.custom { name => calls += 1; name.toUpperCase })
    val names  = KebsCirceConfig.toKindlings(config).transformMemberNames
    names("field") shouldBe "FIELD"
    names("field") shouldBe "FIELD"
    calls shouldBe 1
  }
}
