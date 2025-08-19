package pl.iterators.kebs.jsoniter

import pl.iterators.kebs.core.instances.InstanceConverter
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, JsonWriter, JsonReader}
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import pl.iterators.kebs.core.macros.ValueClassLike

import scala.deriving.Mirror
import scala.util.Try
import scala.util.NotGiven
import ExportedCodecs._

private[jsoniter] trait KebsJsoniterFlatCodec {

  implicit def flatCodec[T, A](using rep: ValueClassLike[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] = {
    new JsonValueCodec[T] {
      override def decodeValue(in: JsonReader, default: T): T = {
        val instanceConvertedDefault: A = Try(rep.unapply(default)).toOption.getOrElse(null.asInstanceOf[A])
        val a                           = codecA.decodeValue(in, instanceConvertedDefault)
        rep.apply(a)
      }
      override def encodeValue(x: T, out: JsonWriter): Unit = {
        codecA.encodeValue(rep.unapply(x), out)
      }
      override val nullValue: T =
        Try(rep.apply(codecA.nullValue)).toOption.getOrElse(null.asInstanceOf[T])
    }
  }

  implicit def instanceConverterCodec[T, A](using rep: InstanceConverter[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] = {
    new JsonValueCodec[T] {
      override def decodeValue(in: JsonReader, default: T): T = {
        val instanceConvertedDefault: A = Try(rep.encode(default)).toOption.getOrElse(null.asInstanceOf[A])
        val a                           = codecA.decodeValue(in, instanceConvertedDefault)
        rep.decode(a)
      }
      override def encodeValue(x: T, out: JsonWriter): Unit = {
        codecA.encodeValue(rep.encode(x), out)
      }
      override val nullValue: T = {
        Try(rep.decode(codecA.nullValue)).toOption.getOrElse(null.asInstanceOf[T])
      }
    }
  }

}

object ExportedCodecs {
  inline implicit def exportCodec[A](using NotGiven[ValueClassLike[A, ?]], NotGiven[InstanceConverter[A, ?]]): JsonValueCodec[A] =
    JsonCodecMaker.make[A](
      CodecMakerConfig
        .withAllowRecursiveTypes(true)
        .withTransientEmpty(false)
        .withTransientNull(false)
        .withTransientNone(false)
    )
}

/* private[jsoniter] trait KebsJsoniterSnakifiedDerivation {

  inline implicit def exportCodec[A](using NotGiven[ValueClassLike[A, ?]], NotGiven[InstanceConverter[A, ?]]): JsonValueCodec[A] =
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

private[jsoniter] trait KebsJsoniterCapitalizedDerivation {

  inline implicit def exportCodec[A](using NotGiven[ValueClassLike[A, ?]], NotGiven[InstanceConverter[A, ?]]): JsonValueCodec[A] =
    JsonCodecMaker.make[A](
      CodecMakerConfig
        .withAllowRecursiveTypes(true)
        .withTransientEmpty(false)
        .withTransientNull(false)
        .withTransientNone(false)
        .withFieldNameMapper(JsonCodecMaker.EnforcePascalCase)
    )
}

trait KebsJsoniterSnakified   extends KebsJsoniterSnakifiedDerivation with KebsJsoniterFlatCodec
trait KebsJsoniterCapitalized extends KebsJsoniterCapitalizedDerivation with KebsJsoniterFlatCodec
*/

trait KebsJsoniter            extends KebsJsoniterFlatCodec
