package pl.iterators.kebs.baklava

import enumeratum.values.{IntEnum, IntEnumEntry}
import enumeratum.{Enum, EnumEntry}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.baklava._
import pl.iterators.kebs.core.enums.ValueEnumLikeEntry
import pl.iterators.kebs.enumeratum.{KebsEnumeratum, KebsValueEnumeratum}

object BaklavaEnumsTests {
  sealed trait Status extends EnumEntry
  object Status       extends Enum[Status] {
    case object Active   extends Status { override val entryName = "is-active" }
    case object Inactive extends Status
    val values = findValues
  }

  sealed abstract class Level(val value: Int) extends IntEnumEntry with ValueEnumLikeEntry[Int]
  object Level                                extends IntEnum[Level] {
    case object Low  extends Level(1)
    case object High extends Level(10)
    val values = findValues
  }

  object BaklavaInstances extends BaklavaQueryParams with BaklavaPathParams with BaklavaHeaders
}

class BaklavaEnumsTests extends AnyFunSuite with Matchers with KebsEnumeratum with KebsValueEnumeratum {
  import BaklavaEnumsTests._
  import BaklavaInstances._

  test("enum schema and params use entryName") {
    import pl.iterators.kebs.baklava.params.enums._
    import pl.iterators.kebs.baklava.schema.enums._

    val schema = implicitly[Schema[Status]]
    schema.className shouldBe classOf[Status].getName
    schema.`type` shouldBe SchemaType.StringType
    schema.format shouldBe None
    schema.`enum` shouldBe Some(Set("is-active", "Inactive"))
    schema.required shouldBe true
    schema.default shouldBe None
    implicitly[ToHeader[Status]].unapply("Active") shouldBe None
    implicitly[ToQueryParam[Status]].apply(Status.Active) shouldBe Seq("is-active")
    implicitly[ToPathParam[Status]].apply(Status.Active) shouldBe "is-active"
    implicitly[ToHeader[Status]].apply(Status.Active) shouldBe Some("is-active")
    implicitly[ToHeader[Status]].unapply("is-active") shouldBe Some(Status.Active)
  }

  test("enum schema and params use entryName - uppercase") {
    import pl.iterators.kebs.baklava.params.enums.uppercase._
    import pl.iterators.kebs.baklava.schema.enums.uppercase._

    implicitly[Schema[Status]].`enum` shouldBe Some(Set("IS-ACTIVE", "INACTIVE"))
    implicitly[ToQueryParam[Status]].apply(Status.Active) shouldBe Seq("IS-ACTIVE")
    implicitly[ToPathParam[Status]].apply(Status.Active) shouldBe "IS-ACTIVE"
    implicitly[ToHeader[Status]].apply(Status.Active) shouldBe Some("IS-ACTIVE")
    implicitly[ToHeader[Status]].unapply("IS-ACTIVE") shouldBe Some(Status.Active)
  }

  test("enum schema and params use entryName - lowercase") {
    import pl.iterators.kebs.baklava.params.enums.lowercase._
    import pl.iterators.kebs.baklava.schema.enums.lowercase._

    implicitly[Schema[Status]].`enum` shouldBe Some(Set("is-active", "inactive"))
    implicitly[ToQueryParam[Status]].apply(Status.Inactive) shouldBe Seq("inactive")
    implicitly[ToPathParam[Status]].apply(Status.Inactive) shouldBe "inactive"
    implicitly[ToHeader[Status]].apply(Status.Inactive) shouldBe Some("inactive")
    implicitly[ToHeader[Status]].unapply("inactive") shouldBe Some(Status.Inactive)
  }

  test("value enum schema uses the value type and values") {
    import pl.iterators.kebs.baklava.schema.enums._

    val schema = implicitly[Schema[Level]]
    schema.className shouldBe classOf[Level].getName
    schema.`type` shouldBe SchemaType.IntegerType
    schema.format shouldBe Some("int32")
    schema.`enum` shouldBe Some(Set("1", "10"))
    schema.default shouldBe None
  }

  test("value enum schema keeps the value type description") {
    import pl.iterators.kebs.baklava.schema.enums._

    val schema = valueEnumLikeSchema[Int, Level](implicitly, Schema.intSchema.withDescription("a level"), implicitly)
    schema.description shouldBe Some("a level")
  }

  test("value enum params use values") {
    import pl.iterators.kebs.baklava.params.enums._

    implicitly[ToQueryParam[Level]].apply(Level.High) shouldBe Seq("10")
    implicitly[ToPathParam[Level]].apply(Level.Low) shouldBe "1"
    implicitly[ToHeader[Level]].apply(Level.High) shouldBe Some("10")
    implicitly[ToHeader[Level]].unapply("10") shouldBe Some(Level.High)
    implicitly[ToHeader[Level]].unapply("5") shouldBe None
  }

  test("value enum instances don't apply to the value type") {
    import pl.iterators.kebs.baklava.schema.enums._

    implicitly[Schema[Int]].`enum` shouldBe None
  }
}
