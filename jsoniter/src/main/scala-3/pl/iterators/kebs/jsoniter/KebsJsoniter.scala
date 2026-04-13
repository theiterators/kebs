package pl.iterators.kebs.jsoniter

import pl.iterators.kebs.core.instances.InstanceConverter
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, JsonWriter, JsonReader}
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.macros.ValueClassLike

import scala.util.Try

private[jsoniter] object KebsJsoniterCodecs {
  def valueClassCodec[T, A](rep: ValueClassLike[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    new JsonValueCodec[T] {
      override def decodeValue(in: JsonReader, default: T): T = {
        val d: A = Try(rep.unapply(default)).toOption.getOrElse(null.asInstanceOf[A])
        rep.apply(codecA.decodeValue(in, d))
      }
      override def encodeValue(x: T, out: JsonWriter): Unit = codecA.encodeValue(rep.unapply(x), out)
      override val nullValue: T                             = Try(rep.apply(codecA.nullValue)).toOption.getOrElse(null.asInstanceOf[T])
    }

  def instanceConverterCodec[T, A](rep: InstanceConverter[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    new JsonValueCodec[T] {
      override def decodeValue(in: JsonReader, default: T): T = {
        val d: A = Try(rep.encode(default)).toOption.getOrElse(null.asInstanceOf[A])
        rep.decode(codecA.decodeValue(in, d))
      }
      override def encodeValue(x: T, out: JsonWriter): Unit = codecA.encodeValue(rep.encode(x), out)
      override val nullValue: T                             = Try(rep.decode(codecA.nullValue)).toOption.getOrElse(null.asInstanceOf[T])
    }
}

private[jsoniter] trait KebsJsoniterDefaultFallback {
  inline implicit def exportCodec[A]: JsonValueCodec[A] =
    JsonCodecMaker.make[A](
      CodecMakerConfig
        .withAllowRecursiveTypes(true)
        .withTransientEmpty(false)
        .withTransientNull(false)
        .withTransientNone(false)
    )
}

private[jsoniter] trait KebsJsoniterSnakifiedFallback {
  inline implicit def exportCodec[A]: JsonValueCodec[A] =
    JsonCodecMaker.make[A](
      CodecMakerConfig
        .withAllowRecursiveTypes(true)
        .withTransientEmpty(false)
        .withTransientNull(false)
        .withTransientNone(false)
        .withAdtLeafClassNameMapper(x => JsonCodecMaker.enforce_snake_case(JsonCodecMaker.simpleClassName(x)))
        .withFieldNameMapper(JsonCodecMaker.enforce_snake_case)
    )
}

private[jsoniter] trait KebsJsoniterSnakifiedBase extends KebsJsoniterSnakifiedFallback {
  implicit def flatCodec[T, A](using rep: ValueClassLike[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    KebsJsoniterCodecs.valueClassCodec(rep, codecA)
  implicit def instanceConverterCodec[T, A](using rep: InstanceConverter[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    KebsJsoniterCodecs.instanceConverterCodec(rep, codecA)
}

private[jsoniter] trait KebsJsoniterCapitalizedFallback {
  inline implicit def exportCodec[A]: JsonValueCodec[A] =
    JsonCodecMaker.make[A](
      CodecMakerConfig
        .withAllowRecursiveTypes(true)
        .withTransientEmpty(false)
        .withTransientNull(false)
        .withTransientNone(false)
        .withFieldNameMapper(JsonCodecMaker.EnforcePascalCase)
    )
}

private[jsoniter] trait KebsJsoniterCapitalizedBase extends KebsJsoniterCapitalizedFallback {
  implicit def flatCodec[T, A](using rep: ValueClassLike[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    KebsJsoniterCodecs.valueClassCodec(rep, codecA)
  implicit def instanceConverterCodec[T, A](using rep: InstanceConverter[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    KebsJsoniterCodecs.instanceConverterCodec(rep, codecA)
}

private[jsoniter] trait KebsJsoniterDefaultBase extends KebsJsoniterDefaultFallback {
  implicit def flatCodec[T, A](using rep: ValueClassLike[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    KebsJsoniterCodecs.valueClassCodec(rep, codecA)
  implicit def instanceConverterCodec[T, A](using rep: InstanceConverter[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    KebsJsoniterCodecs.instanceConverterCodec(rep, codecA)
}

trait KebsJsoniter            extends KebsJsoniterDefaultBase
trait KebsJsoniterSnakified   extends KebsJsoniterSnakifiedBase
trait KebsJsoniterCapitalized extends KebsJsoniterCapitalizedBase
