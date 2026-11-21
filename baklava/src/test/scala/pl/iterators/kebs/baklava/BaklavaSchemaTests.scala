package pl.iterators.kebs.baklava

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.baklava.{Schema, SchemaType}
import pl.iterators.kebs.baklava.schema.KebsBaklavaSchema
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pl.iterators.kebs.instances.net.URIString
import pl.iterators.kebs.instances.time.{DurationString, InstantString, LocalDateString, YearMonthString, ZonedDateTimeString}
import pl.iterators.kebs.instances.util.UUIDString

import java.net.URI
import java.time.{Duration, Instant, LocalDate, YearMonth, ZonedDateTime}
import java.util.UUID

object BaklavaSchemaTests {
  case class UserName(value: String)
  case class Age(value: Int)
  case class Tags(value: Map[String, Int])
}

class BaklavaSchemaTests
    extends AnyFunSuite
    with Matchers
    with KebsBaklavaSchema
    with CaseClass1ToValueClass
    with InstantString
    with LocalDateString
    with ZonedDateTimeString
    with YearMonthString
    with DurationString
    with URIString
    with UUIDString {
  import BaklavaSchemaTests._

  test("value class schema follows the underlying type") {
    val userName = implicitly[Schema[UserName]]
    userName.className shouldBe classOf[UserName].getName
    userName.`type` shouldBe SchemaType.StringType
    userName.format shouldBe None
    userName.`enum` shouldBe None
    userName.required shouldBe true

    val age = implicitly[Schema[Age]]
    age.`type` shouldBe SchemaType.IntegerType
    age.format shouldBe Some("int32")
  }

  test("value class schema keeps the underlying default and description") {
    val base   = Schema.stringSchema.withDefault("joe").withDescription("a user name")
    val schema = valueClassLikeSchema[UserName, String](implicitly, base, implicitly)
    schema.default shouldBe Some(UserName("joe"))
    schema.description shouldBe Some("a user name")
  }

  test("value class schema keeps the underlying map value schema") {
    val schema = implicitly[Schema[Tags]]
    schema.`type` shouldBe SchemaType.ObjectType
    schema.additionalProperties shouldBe true
    schema.additionalPropertiesSchema.map(_.`type`) shouldBe Some(SchemaType.IntegerType)
  }

  test("instance converter schema follows the encoded type, with well-known formats") {
    implicitly[Schema[LocalDate]].format shouldBe Some("date")
    implicitly[Schema[Instant]].format shouldBe Some("date-time")
    implicitly[Schema[ZonedDateTime]].format shouldBe Some("date-time")
    implicitly[Schema[URI]].format shouldBe Some("uri")
    implicitly[Schema[UUID]].format shouldBe Some("uuid")

    val yearMonth = implicitly[Schema[YearMonth]]
    yearMonth.className shouldBe classOf[YearMonth].getName
    yearMonth.`type` shouldBe SchemaType.StringType
    yearMonth.format shouldBe None
    implicitly[Schema[Duration]].format shouldBe None
  }

  test("instance converter schema keeps the encoded default and description") {
    val base   = Schema.stringSchema.withDefault("2024-05").withDescription("a month")
    val schema = instanceConverter[YearMonth, String](implicitly, base, implicitly)
    schema.default shouldBe Some(YearMonth.of(2024, 5))
    schema.description shouldBe Some("a month")
  }
}
