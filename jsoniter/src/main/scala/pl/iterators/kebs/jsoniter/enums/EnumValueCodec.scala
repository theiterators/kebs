package pl.iterators.kebs.jsoniter.enums

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import pl.iterators.kebs.jsoniter.internal.JsoniterCodecs

// not a JsonValueCodec: with an implicit one of String or Int, pekko-http would send strings and numbers as JSON
final class EnumValueCodec[V](val codec: JsonValueCodec[V])

object EnumValueCodec extends StandardEnumValueCodecs {
  implicit def existing[V](implicit codec: JsonValueCodec[V]): EnumValueCodec[V] = new EnumValueCodec(codec)
}

private[enums] trait StandardEnumValueCodecs {
  implicit def standard[V](implicit codec: StandardEnumValueCodec[V]): EnumValueCodec[V] = new EnumValueCodec(codec.codec)
}

final class StandardEnumValueCodec[V] private (val codec: JsonValueCodec[V])

object StandardEnumValueCodec {
  implicit val string: StandardEnumValueCodec[String] = new StandardEnumValueCodec(JsoniterCodecs.string)
  implicit val int: StandardEnumValueCodec[Int]       = new StandardEnumValueCodec(JsoniterCodecs.int)
  implicit val long: StandardEnumValueCodec[Long]     = new StandardEnumValueCodec(JsoniterCodecs.long)
  implicit val short: StandardEnumValueCodec[Short]   = new StandardEnumValueCodec(JsoniterCodecs.short)
  implicit val byte: StandardEnumValueCodec[Byte]     = new StandardEnumValueCodec(JsoniterCodecs.byte)
  implicit val char: StandardEnumValueCodec[Char]     = new StandardEnumValueCodec(JsoniterCodecs.char)
}
