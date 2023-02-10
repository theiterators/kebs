package pl.iterators.kebs.instances.util

import pl.iterators.kebs.core.InstanceConverter
import pl.iterators.kebs.instances.util.CurrencyString.CurrencyFormat

import java.util.Currency

trait CurrencyString {
  implicit val currencyFormatter: InstanceConverter[Currency, String] =
    InstanceConverter[Currency, String](_.toString, Currency.getInstance, Some(CurrencyFormat))
}
object CurrencyString {
  private[instances] val CurrencyFormat = "ISO-4217 standard format e.g. USD"
}
