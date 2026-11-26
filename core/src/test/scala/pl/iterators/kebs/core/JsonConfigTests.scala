package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}
import pl.iterators.kebs.core.naming.Naming

class JsonConfigTests extends AnyFunSuite with Matchers {
  test("defaults") {
    JsonConfig.default shouldBe JsonConfig(
      Naming.identity,
      Naming.identity,
      NoneHandling.WriteNull,
      useDefaults = false,
      discriminator = None
    )
  }

  test("each builder sets exactly its field") {
    val base = JsonConfig.default
    base.withFieldNaming(Naming.snakeCase) shouldBe base.copy(fieldNaming = Naming.snakeCase)
    base.withConstructorNaming(Naming.kebabCase) shouldBe base.copy(constructorNaming = Naming.kebabCase)
    base.withNoneHandling(NoneHandling.Omit) shouldBe base.copy(noneHandling = NoneHandling.Omit)
    base.withDefaults shouldBe base.copy(useDefaults = true)
    base.withDiscriminator("type") shouldBe base.copy(discriminator = Some("type"))
  }
}
