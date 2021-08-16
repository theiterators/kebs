package pl.iterators.kebs.instances

import pl.iterators.kebs.instances.util.{CurrencyString, LocaleString, UUIDString}

trait UtilInstances extends CurrencyString with LocaleString with UUIDString

object UtilInstances extends UtilInstances
