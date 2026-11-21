package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.namingconventions.SnakifyVariant
import pl.iterators.kebs.core.naming.Naming

class NamingTests extends AnyFunSuite with Matchers {
  private val words = List("someField", "field1Name", "httpURL", "HTTPServer", "InProgress", "already_snake")

  test("snakeCase is kebs' snakify") {
    words.map(Naming.snakeCase(_)) shouldBe words.map(SnakifyVariant.snakify)
    words.map(Naming.snakeCase(_)) shouldBe List("some_field", "field1_name", "http_url", "http_server", "in_progress", "already_snake")
  }

  test("screaming snake, kebab and camel case") {
    Naming.screamingSnakeCase("InProgress") shouldBe "IN_PROGRESS"
    Naming.kebabCase("InProgress") shouldBe "in-progress"
    Naming.camelCase("InProgress") shouldBe "inProgress"
    Naming.camelCase("in_progress") shouldBe "inProgress"
  }

  test("pascal case, upper and lower case") {
    Naming.pascalCase("someField") shouldBe "SomeField"
    Naming.uppercase("DarkBlue") shouldBe "DARKBLUE"
    Naming.lowercase("DarkBlue") shouldBe "darkblue"
    Naming.identity("DarkBlue") shouldBe "DarkBlue"
  }

  test("styles compose and custom ones are allowed") {
    (Naming.snakeCase andThen Naming.uppercase)("InProgress") shouldBe "IN_PROGRESS"
    Naming.custom(_.reverse)("abc") shouldBe "cba"
  }

  test("a name is transformed once per naming") {
    var calls  = 0
    val naming = Naming.custom { name => calls += 1; name.reverse }
    naming("abc") shouldBe "cba"
    naming("abc") shouldBe "cba"
    calls shouldBe 1
  }
}
