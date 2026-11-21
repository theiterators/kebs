package pl.iterators.kebs.circe.internal

import hearth.kindlings.circederivation.Configuration
import pl.iterators.kebs.core.json.{JsonConfig, NoneHandling}

object KebsCirceConfig {
  def toKindlings(config: JsonConfig): Configuration = {
    // kindlings' circe derivation cannot drop None fields yet; failing beats silently writing nulls
    if (config.noneHandling == NoneHandling.Omit)
      throw new IllegalArgumentException("NoneHandling.Omit is not supported by kebs-circe yet")
    Configuration(
      transformMemberNames = config.fieldNaming(_),
      transformConstructorNames = config.constructorNaming(_),
      useDefaults = config.useDefaults,
      discriminator = config.discriminator,
      unwrapValueClasses = false
    )
  }
}
