package pl.iterators.kebs.instances

import pl.iterators.kebs.macros.CaseClass1Rep

import java.util.{Currency, Locale, UUID}

trait UtilInstances {

  implicit val currencyClass1Rep: CaseClass1Rep[Currency, String] =
    new CaseClass1Rep[Currency, String](Currency.getInstance, _.toString)

  implicit val localeClass1Rep: CaseClass1Rep[Locale, String] =
    new CaseClass1Rep[Locale, String](Locale.forLanguageTag, _.toLanguageTag)

  implicit val uuidCaseClass1Rep: CaseClass1Rep[UUID, String] =
    new CaseClass1Rep[UUID, String](UUID.fromString, _.toString)
}
