package pl.iterators.kebs.jsoniter.enums

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import com.github.plokhotnyuk.jsoniter_scala.core._

trait KebsJsoniterEnums {
  protected final def enumJsonValueCodec[E](
      `enum`: EnumLike[E],
      map: E => String,
      comap: String => Option[E]
  ): JsonValueCodec[E] =
    new JsonValueCodec[E] {
      override def decodeValue(in: JsonReader, default: E): E = {
        val stringValue = in.readString(null)
        comap(stringValue).getOrElse {
          val enumNames = `enum`.getNamesToValuesMap.values.mkString(", ")
          in.decodeError(s"$stringValue should be one of $enumNames")
        }
      }
      override def encodeValue(x: E, out: JsonWriter): Unit = out.writeVal(map(x))
      override def nullValue: E                             = null.asInstanceOf[E]
    }

  def defaultEnumJsonValueCodec[E](`enum`: EnumLike[E]): JsonValueCodec[E] =
    enumJsonValueCodec[E](`enum`, _.toString, `enum`.withNameInsensitiveOption(_))

  def uppercaseEnumJsonValueCodec[E](`enum`: EnumLike[E]): JsonValueCodec[E] =
    enumJsonValueCodec[E](`enum`, _.toString.toUpperCase, `enum`.withNameUppercaseOnlyOption(_))

  def lowercaseEnumJsonValueCodec[E](`enum`: EnumLike[E]): JsonValueCodec[E] =
    enumJsonValueCodec[E](`enum`, _.toString.toLowerCase, `enum`.withNameLowercaseOnlyOption(_))

  implicit def enumCodec[E](implicit ev: EnumLike[E]): JsonValueCodec[E] = defaultEnumJsonValueCodec(ev)
}

trait KebsJsoniterEnumsUppercase extends KebsJsoniterEnums {
  override implicit def enumCodec[E](implicit ev: EnumLike[E]): JsonValueCodec[E] = uppercaseEnumJsonValueCodec(ev)
}

trait KebsJsoniterEnumsLowercase extends KebsJsoniterEnums {
  override implicit def enumCodec[E](implicit ev: EnumLike[E]): JsonValueCodec[E] = lowercaseEnumJsonValueCodec(ev)
}

trait KebsJsoniterValueEnums {
  def valueEnumJsonValueCodec[V, E <: ValueEnumLikeEntry[V]](
      `enum`: ValueEnumLike[V, E]
  )(implicit baseCodec: JsonValueCodec[V]): JsonValueCodec[E] =
    new JsonValueCodec[E] {
      override def decodeValue(in: JsonReader, default: E): E = {
        val value = baseCodec.decodeValue(in, baseCodec.nullValue)
        `enum`.withValueOption(value).getOrElse {
          val enumValues = `enum`.getValuesToEntriesMap.keys.mkString(", ")
          in.decodeError(s"$value is not a member of $enumValues")
        }
      }
      override def encodeValue(x: E, out: JsonWriter): Unit = baseCodec.encodeValue(x.value, out)
      override def nullValue: E                             = null.asInstanceOf[E]
    }

  implicit def valueEnumCodec[V, E <: ValueEnumLikeEntry[V]](implicit
      ev: ValueEnumLike[V, E],
      baseCodec: JsonValueCodec[V]
  ): JsonValueCodec[E] = valueEnumJsonValueCodec(ev)
}
