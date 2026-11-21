package pl.iterators.kebs.baklava

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.baklava._
import pl.iterators.kebs.baklava.params.KebsBaklavaParams
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.instances.time.LocalDateString

import java.time.LocalDate

object BaklavaParamsTests {
  case class UserName(value: String)
  case class Age(value: Int)
}

class BaklavaParamsTests
    extends AnyFunSuite
    with Matchers
    with KebsBaklavaParams
    with CaseClass1ToValueClass
    with LocalDateString
    with BaklavaQueryParams
    with BaklavaPathParams
    with BaklavaHeaders {
  import BaklavaParamsTests._

  private val date = LocalDate.of(2024, 5, 17)

  test("value class query params use the underlying value") {
    implicitly[ToQueryParam[UserName]].apply(UserName("joe")) shouldBe Seq("joe")
    implicitly[ToQueryParam[Age]].apply(Age(42)) shouldBe Seq("42")
  }

  test("value class path params use the underlying value") {
    implicitly[ToPathParam[UserName]].apply(UserName("joe")) shouldBe "joe"
    implicitly[ToPathParam[Age]].apply(Age(42)) shouldBe "42"
  }

  test("value class headers use the underlying value") {
    implicitly[ToHeader[UserName]].apply(UserName("joe")) shouldBe Some("joe")
    implicitly[ToHeader[UserName]].unapply("joe") shouldBe Some(UserName("joe"))
    implicitly[ToHeader[Age]].apply(Age(42)) shouldBe Some("42")
    implicitly[ToHeader[Age]].unapply("42") shouldBe Some(Age(42))
    implicitly[ToHeader[Age]].unapply("not a number") shouldBe None
  }

  test("instance converter query and path params use the encoded value") {
    implicitly[ToQueryParam[LocalDate]].apply(date) shouldBe Seq("2024-05-17")
    implicitly[ToPathParam[LocalDate]].apply(date) shouldBe "2024-05-17"
  }

  test("instance converter headers use the encoded value") {
    implicitly[ToHeader[LocalDate]].apply(date) shouldBe Some("2024-05-17")
    implicitly[ToHeader[LocalDate]].unapply("2024-05-17") shouldBe Some(date)
  }
}
