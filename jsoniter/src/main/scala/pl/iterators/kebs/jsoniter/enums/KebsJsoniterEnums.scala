/* package pl.iterators.kebs.jsoniter.enums

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

trait KebsJsoniterEnums {
  @inline protected final def enumNameDeserializationError[E](`enum`: EnumLike[E], name: String): String = {
    val enumNames = `enum`.getNamesToValuesMap.values.mkString(", ")
    s"$name should be one of $enumNames"
  }

  @inline protected final def enumValueDeserializationError[E](`enum`: EnumLike[E], value: String): String = {
    val enumNames = `enum`.getNamesToValuesMap.values.mkString(", ")
    s"$value should be a string of value $enumNames"
  }

  protected final def enumJsonValueCodec[E](`enum`: EnumLike[E], map: E => String, comap: String => Option[E]): JsonValueCodec[E] = {
    new JsonValueCodec[E] {
      override def decodeValue(in: JsonReader, default: E): E = {
        val stringValue = in.readString(null)
        comap(stringValue).getOrElse {
          val enumValues = `enum`.valuesToNamesMap.values.mkString(", ")
          throw new JsoniterReaderExceptionImpl(s"$stringValue is not a member of enum values: $enumValues")
        }
      }

      override def encodeValue(x: E, out: JsonWriter): Unit = {
        val stringValue = map(x)
        out.writeVal(stringValue)
      }

      override def nullValue: E = null.asInstanceOf[E]
    }
  }

  def enumJsonValueCodec[E](`enum`: EnumLike[E]): JsonValueCodec[E] =
    enumJsonValueCodec[E](`enum`, _.toString, `enum`.withNameInsensitiveOption(_))

  def uppercaseEnumJsonValueCodec[E](`enum`: EnumLike[E]): JsonValueCodec[E] =
    enumJsonValueCodec[E](`enum`, _.toString.toUpperCase, `enum`.withNameUppercaseOnlyOption(_))

  def lowercaseEnumJsonValueCodec[E](`enum`: EnumLike[E]): JsonValueCodec[E] =
    enumJsonValueCodec[E](`enum`, _.toString.toLowerCase, `enum`.withNameLowercaseOnlyOption(_))

  implicit def enumCodecImpl[E](implicit ev: EnumLike[E]): JsonValueCodec[E] = enumJsonValueCodec(ev)

  trait KebsJsoniterEnumsUppercase {
    implicit def enumJsonValueCodecImpl[E](implicit ev: EnumLike[E]): JsonValueCodec[E] =
      uppercaseEnumJsonValueCodec(ev)
  }

  trait KebsJsoniterEnumsLowercase {
    implicit def enumJsonValueCodecImpl[E](implicit ev: EnumLike[E]): JsonValueCodec[E] =
      lowercaseEnumJsonValueCodec(ev)
  }
}

trait KebsJsoniterValueEnums {
  sealed trait NotGiven[A]

  object NotGiven extends LowPriorityNotGiven {
    implicit def amb1[A](implicit ev: A): NotGiven[A] = sys.error("should not be called")
    implicit def amb2[A](implicit ev: A): NotGiven[A] = sys.error("should not be called")
  }

  trait LowPriorityNotGiven {
    implicit def notFound[A]: NotGiven[A] = new NotGiven[A] {}
  }

  inline implicit def exportCodec[E]: JsonValueCodec[E] = {
    JsonCodecMaker.make[E](
      CodecMakerConfig
        .withDiscriminatorFieldName(None)
        .withAllowRecursiveTypes(true)
        .withTransientEmpty(false)
        .withTransientNull(false)
        .withTransientNone(false)
    )
  }

  @inline protected final def valueEnumDeserializationError[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E], value: V) = {
    val enumValues = `enum`.getValuesToEntriesMap.keys.mkString(", ")
    throw new JsoniterReaderExceptionImpl(s"$value is not a member of $enumValues")
  }

  def valueEnumCodec[V, E <: ValueEnumLikeEntry[V]](`enum`: ValueEnumLike[V, E])(implicit baseJsonCodec: JsonValueCodec[V]) =
    new JsonValueCodec[E] {
      override def decodeValue(in: JsonReader, default: E): E = {
        val value = baseJsonCodec.decodeValue(in, baseJsonCodec.nullValue)
        `enum`.withValueOption(value).getOrElse(valueEnumDeserializationError(`enum`, value))
      }
      override def encodeValue(x: E, out: JsonWriter): Unit = {
        baseJsonCodec.encodeValue(x.value, out)
      }
      override def nullValue: E = null.asInstanceOf[E]
    }

  implicit def valueEnumCodecImpl[V, E <: ValueEnumLikeEntry[V]](implicit
      ev: ValueEnumLike[V, E],
      codec: JsonValueCodec[V]
  ): JsonValueCodec[E] =
    valueEnumCodec(ev)
}

class JsoniterReaderExceptionImpl(reason: String) extends Exception {
  override def getMessage(): String = reason
}
 */
