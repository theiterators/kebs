package pl.iterators.kebs.instances.util

import CurrencyString.CurrencyFormat
import pl.iterators.kebs.core.instances.InstanceConverter

import java.util.Currency

trait CurrencyString {
  implicit val currencyFormatter: InstanceConverter[Currency, String] =
    InstanceConverter[Currency, String](_.toString, Currency.getInstance, Some(CurrencyFormat))
}
object CurrencyString {
  private[instances] val CurrencyFormat = "ISO-4217 standard format e.g. USD"
}
