package pl.iterators.kebs.instances

import pl.iterators.kebs.macros.CaseClass1Rep

import java.util.{Currency, Locale, UUID}

trait UtilInstances extends Instances {

  implicit def currencyRep[T](implicit f: InstancesFormatter[Currency, T]): CaseClass1Rep[Currency, T] =
    new CaseClass1Rep[Currency, T](decodeObject[Currency, T](f.decode), f.encode)

  implicit def localeRep[T](implicit f: InstancesFormatter[Locale, T]): CaseClass1Rep[Locale, T] =
    new CaseClass1Rep[Locale, T](decodeObject[Locale, T](f.decode), f.encode)

  implicit def uuidRep[T](implicit f: InstancesFormatter[UUID, T]): CaseClass1Rep[UUID, T] =
    new CaseClass1Rep[UUID, T](decodeObject[UUID, T](f.decode), f.encode)
}

object UtilInstances {
  private[instances] val CurrencyFormat = "ISO-4217 standard format e.g. PLN"
  private[instances] val LocaleFormat   = "IETF BCP 47 standard format e.g. pl-PL" // Note: Locale.toString() does not throw
  private[instances] val UUIDFormat     = "128-bit number e.g. 123e4567-e89b-12d3-a456-426614174000"

  trait CurrencyString extends UtilInstances {
    implicit val currencyFormatter: InstancesFormatter[Currency, String] = new InstancesFormatter[Currency, String] {
      override def encode(obj: Currency): String = obj.toString
      override def decode(value: String): Either[DecodeError, Currency] =
        tryParse[Currency, String](Currency.getInstance, value, classOf[Currency], CurrencyFormat)
    }
  }

  trait LocaleString extends UtilInstances {
    implicit val localeFormatter: InstancesFormatter[Locale, String] = new InstancesFormatter[Locale, String] {
      override def encode(obj: Locale): String = obj.toLanguageTag
      override def decode(value: String): Either[DecodeError, Locale] =
        tryParse[Locale, String](Locale.forLanguageTag, value, classOf[Locale], LocaleFormat)
    }
  }

  trait UUIDString extends UtilInstances {
    implicit val uuidFormatter: InstancesFormatter[UUID, String] = new InstancesFormatter[UUID, String] {
      override def encode(obj: UUID): String = obj.toString
      override def decode(value: String): Either[DecodeError, UUID] =
        tryParse[UUID, String](UUID.fromString, value, classOf[UUID], UUIDFormat)
    }
  }
}
