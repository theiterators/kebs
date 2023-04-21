package instances

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.circe.KebsCirce
import pl.iterators.kebs.instances.InstanceConverter.DecodeErrorException
import pl.iterators.kebs.instances.UtilInstances

import java.util.{Currency, Locale, UUID}

class UtilInstancesTests extends AnyFunSuite with Matchers with KebsCirce with UtilInstances {

  test("No CaseClass1Rep implicits derived") {

    "implicitly[CaseClass1Rep[Currency, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, Currency]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[Locale, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, Locale]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[UUID, String]]" shouldNot typeCheck
    "implicitly[CaseClass1Rep[String, UUID]]" shouldNot typeCheck
  }

  test("Currency standard format") {
    val encoder    = implicitly[Encoder[Currency]]
    val decoder    = implicitly[Decoder[Currency]]
    val value = "PLN"
    val obj   = Currency.getInstance(value)

    encoder(obj) shouldBe Json.fromString(value)
    decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
  }

  test("Currency wrong format exception") {
    val decoder    = implicitly[Decoder[Currency]]
    val value = "not a Currency"

    decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
  }

   test("Locale standard format") {
     val encoder    = implicitly[Encoder[Locale]]
     val decoder    = implicitly[Decoder[Locale]]
     val value = "pl-PL"
     val obj   = Locale.forLanguageTag(value)

     encoder(obj) shouldBe Json.fromString(value)
     decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
   }

  test("UUID standard format") {
    val encoder    = implicitly[Encoder[UUID]]
    val decoder    = implicitly[Decoder[UUID]]
    val value = "123e4567-e89b-12d3-a456-426614174000"
    val obj   = UUID.fromString(value)

    encoder(obj) shouldBe Json.fromString(value)
    decoder(Json.fromString(value).hcursor) shouldBe Right(obj)
  }

  test("UUID wrong format exception") {
    val decoder    = implicitly[Decoder[UUID]]
    val value = "not an UUID"

    decoder(Json.fromString(value).hcursor) shouldBe a [Left[_, _]]
  }
}
