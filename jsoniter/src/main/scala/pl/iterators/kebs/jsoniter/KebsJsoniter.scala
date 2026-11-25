package pl.iterators.kebs.jsoniter

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.naming.Naming

trait KebsJsoniter extends KebsJsoniterAutoCompat {
  // read through the protocol, not implicitly: another protocol imported in the same scope would make it ambiguous
  def kebsJsonConfig: JsonConfig = JsonConfig.default

  trait KebsJsoniterSnakified extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.snakeCase)
  }

  trait KebsJsoniterCapitalized extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.pascalCase)
  }
}
