package pl.iterators.kebs.jsoniter.internal

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.jsoniter.KebsJsoniterMacros

import scala.language.experimental.macros

// called by the code the entry points generate
object JsoniterLocalCodecs {
  def level[T](config: JsonConfig): JsonValueCodec[T] = macro KebsJsoniterMacros.level[T]
}
