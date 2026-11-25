package pl.iterators.kebs.jsoniter.enums

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import pl.iterators.kebs.core.enums.{EnumLike, EnumNaming, ValueEnumLike, ValueEnumLikeEntry}

trait KebsJsoniterEnums {
  // mixing the base trait with a preset must not compile (it would silently pick one of them)
  protected def kebsEnumCodecs: Unit = ()

  def namedEnumCodec[E](`enum`: EnumLike[E], naming: EnumNaming): JsonValueCodec[E] = {
    val named = naming.applyTo(`enum`)
    new JsonValueCodec[E] {
      def decodeValue(in: JsonReader, default: E): E = {
        val name = in.readString(null)
        if (name eq null) in.decodeError("unexpected null")
        naming.lookup(named, name).getOrElse(in.decodeError(s"$name should be one of ${named.names.mkString(", ")}"))
      }
      def encodeValue(x: E, out: JsonWriter): Unit = out.writeVal(named.getName(x))
      def nullValue: E                             = null.asInstanceOf[E]
    }
  }

  def enumCodec[E](`enum`: EnumLike[E]): JsonValueCodec[E]          = namedEnumCodec(`enum`, EnumNaming.default)
  def uppercaseEnumCodec[E](`enum`: EnumLike[E]): JsonValueCodec[E] = namedEnumCodec(`enum`, EnumNaming.uppercase)
  def lowercaseEnumCodec[E](`enum`: EnumLike[E]): JsonValueCodec[E] = namedEnumCodec(`enum`, EnumNaming.lowercase)

  implicit def enumCodecImpl[E](implicit ev: EnumLike[E], naming: EnumNaming): JsonValueCodec[E] = namedEnumCodec(ev, naming)

  trait KebsJsoniterEnumsUppercase {
    protected def kebsEnumCodecs: Unit = ()

    implicit def enumCodecImpl[E](implicit ev: EnumLike[E]): JsonValueCodec[E] = uppercaseEnumCodec(ev)
  }

  trait KebsJsoniterEnumsLowercase {
    protected def kebsEnumCodecs: Unit = ()

    implicit def enumCodecImpl[E](implicit ev: EnumLike[E]): JsonValueCodec[E] = lowercaseEnumCodec(ev)
  }
}

trait KebsJsoniterValueEnums {
  def valueEnumCodec[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit value: EnumValueCodec[V]): JsonValueCodec[E] =
    new JsonValueCodec[E] {
      def decodeValue(in: JsonReader, default: E): E = {
        val read = value.codec.decodeValue(in, value.codec.nullValue)
        `enum`.withValueOption(read).getOrElse {
          in.decodeError(s"$read is not a member of ${`enum`.getValuesToEntriesMap.keys.mkString(", ")}")
        }
      }
      def encodeValue(x: E, out: JsonWriter): Unit = value.codec.encodeValue(x.value, out)
      def nullValue: E                             = null.asInstanceOf[E]
    }

  implicit def valueEnumCodecImpl[V, E <: ValueEnumLikeEntry[V]](implicit
      ev: ValueEnumLike[V, E],
      value: EnumValueCodec[V]
  ): JsonValueCodec[E] = valueEnumCodec(ev)
}
