package pl.iterators.kebs.jsoniter

import pl.iterators.kebs.core.instances.InstanceConverter
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import pl.iterators.kebs.core.macros.ValueClassLike
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

import scala.util.Try

private[jsoniter] trait KebsJsoniterFlatCodec {
  import ExportedCodecs._
  implicit def flatCodec[T, A](implicit rep: ValueClassLike[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] = {
    new JsonValueCodec[T] {
      override def decodeValue(in: JsonReader, default: T): T = {
        val a = codecA.decodeValue(in, rep.unapply(default))
        rep.apply(a)
      }
      override def encodeValue(x: T, out: JsonWriter): Unit = {
        codecA.encodeValue(rep.unapply(x), out)
      }
      override val nullValue: T = rep.apply(codecA.nullValue)
    }
  }

  implicit def instanceConverterCodec[T, A](implicit rep: InstanceConverter[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] = {
    new JsonValueCodec[T] {
      override def decodeValue(in: JsonReader, default: T): T = {
        val a = codecA.decodeValue(in, rep.encode(default))
        rep.decode(a)
      }
      override def encodeValue(x: T, out: JsonWriter): Unit = {
        codecA.encodeValue(rep.encode(x), out)
      }
      override val nullValue: T = rep.decode(codecA.nullValue)
    }
  }

}

object ExportedCodecs {
  implicit def exportCodec[A]: JsonValueCodec[A] = macro exportCodecImpl[A]

  def exportCodecImpl[A: c.WeakTypeTag](c: blackbox.Context): c.Expr[JsonValueCodec[A]] = {
    import c.universe._
    c.Expr[JsonValueCodec[A]](
      q"""_root_.com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker.make[$weakTypeOf[A]](
          _root_.com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig.withAllowRecursiveTypes(true)
       )"""
    )
  }
}

trait KebsJsoniter extends KebsJsoniterFlatCodec
