package pl.iterators.kebs.jsoniter

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.naming.Naming

// all of KebsJsoniter but its implicit derivation: codecs come from deriveCodec
trait KebsJsoniterSemiauto extends KebsJsoniterDeriveCompat {
  // read through the protocol, not implicitly: another protocol imported in the same scope would make it ambiguous
  def kebsJsonConfig: JsonConfig = JsonConfig.default
}

trait KebsJsoniter extends KebsJsoniterSemiauto with KebsJsoniterAutoCompat {
  trait KebsJsoniterSnakified extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.snakeCase)
  }

  trait KebsJsoniterCapitalized extends KebsJsoniter {
    override def kebsJsonConfig: JsonConfig = super.kebsJsonConfig.withFieldNaming(Naming.pascalCase)
  }
}
