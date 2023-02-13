package pl.iterators.kebs.instances.util

import pl.iterators.kebs.converters.InstanceConverter
import pl.iterators.kebs.instances.util.LocaleString.LocaleFormat

import java.util.Locale

trait LocaleString {
  implicit val localeFormatter: InstanceConverter[Locale, String] =
    InstanceConverter[Locale, String](_.toLanguageTag, Locale.forLanguageTag, Some(LocaleFormat))
}
object LocaleString {
  private[instances] val LocaleFormat = "IETF BCP 47 standard format e.g. en-US"
}
