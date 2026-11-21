package pl.iterators.kebs.core.json

import pl.iterators.kebs.core.naming.Naming

sealed trait NoneHandling
object NoneHandling {
  case object WriteNull extends NoneHandling
  case object Omit      extends NoneHandling
}

final case class JsonConfig(
    fieldNaming: Naming = Naming.identity,
    constructorNaming: Naming = Naming.identity,
    noneHandling: NoneHandling = NoneHandling.WriteNull,
    useDefaults: Boolean = false,
    discriminator: Option[String] = None
) {
  def withFieldNaming(naming: Naming): JsonConfig          = copy(fieldNaming = naming)
  def withConstructorNaming(naming: Naming): JsonConfig    = copy(constructorNaming = naming)
  def withNoneHandling(handling: NoneHandling): JsonConfig = copy(noneHandling = handling)
  def withDefaults: JsonConfig                             = copy(useDefaults = true)
  def withDiscriminator(field: String): JsonConfig         = copy(discriminator = Some(field))
}

object JsonConfig {
  val default: JsonConfig = JsonConfig()
}
