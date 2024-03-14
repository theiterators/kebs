package pl.iterators.kebs.instances.util

import LocaleString.LocaleFormat
import pl.iterators.kebs.core.instances.InstanceConverter

import java.util.Locale

trait LocaleString {
  implicit val localeFormatter: InstanceConverter[Locale, String] =
    InstanceConverter[Locale, String](_.toLanguageTag, Locale.forLanguageTag, Some(LocaleFormat))
}
object LocaleString {
  private[instances] val LocaleFormat = "IETF BCP 47 standard format e.g. en-US"
}
