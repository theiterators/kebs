package pl.iterators.kebs.jsoniter

import pl.iterators.kebs.core.instances.InstanceConverter
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import pl.iterators.kebs.core.macros.ValueClassLike
import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.Try

private[jsoniter] final class KebsValueClassCodec[T, A](
    val rep: ValueClassLike[T, A],
    val codecA: JsonValueCodec[A]
) extends JsonValueCodec[T] {
  override def decodeValue(in: JsonReader, default: T): T = {
    val d: A = Try(rep.unapply(default)).toOption.getOrElse(null.asInstanceOf[A])
    rep.apply(codecA.decodeValue(in, d))
  }
  override def encodeValue(x: T, out: JsonWriter): Unit =
    codecA.encodeValue(rep.unapply(x), out)
  override val nullValue: T =
    Try(rep.apply(codecA.nullValue)).toOption.getOrElse(null.asInstanceOf[T])
}

private[jsoniter] final class KebsInstanceConverterCodec[T, A](
    val rep: InstanceConverter[T, A],
    val codecA: JsonValueCodec[A]
) extends JsonValueCodec[T] {
  override def decodeValue(in: JsonReader, default: T): T = {
    val d: A = Try(rep.encode(default)).toOption.getOrElse(null.asInstanceOf[A])
    rep.decode(codecA.decodeValue(in, d))
  }
  override def encodeValue(x: T, out: JsonWriter): Unit =
    codecA.encodeValue(rep.encode(x), out)
  override val nullValue: T =
    Try(rep.decode(codecA.nullValue)).toOption.getOrElse(null.asInstanceOf[T])
}

object KebsJsoniterMacros {
  private def exportCodecWithConfig[A: c.WeakTypeTag](c: blackbox.Context)(configTree: c.universe.Tree): c.Expr[JsonValueCodec[A]] = {
    import c.universe._
    val tpe = weakTypeOf[A]

    val vclTpe = c.typecheck(tq"_root_.pl.iterators.kebs.core.macros.ValueClassLike[$tpe, _]", c.TYPEmode).tpe
    val icTpe  = c.typecheck(tq"_root_.pl.iterators.kebs.core.instances.InstanceConverter[$tpe, _]", c.TYPEmode).tpe

    val isKebsType = c.inferImplicitValue(vclTpe).nonEmpty || c.inferImplicitValue(icTpe).nonEmpty

    if (isKebsType) {
      c.abort(c.enclosingPosition, s"$tpe is a Kebs type; skipping deriveCodec")
    }

    // Use a proxy pattern to handle recursive types (e.g. case class R(a: Int, rs: Seq[R])).
    //
    // When JsonCodecMaker.make[R] expands and processes the field `rs: Seq[R]`, it does an
    // implicit search for JsonValueCodec[R].  Without special handling it would find the Kebs
    // `deriveCodec[R]` macro again, which would re-expand JsonCodecMaker.make[R], causing infinite
    // compile-time recursion.
    //
    // The fix: introduce a local `implicit val` proxy for JsonValueCodec[A] in the same block.
    // Local implicits take priority over inherited/imported ones, so JsonCodecMaker.make[A] finds
    // the proxy instead of `deriveCodec[A]` for any recursive self-references.  After
    // JsonCodecMaker.make[A] completes and the real codec is stored in codecVar, the proxy
    // delegates to it at runtime — giving correct recursive behaviour.
    val codecVar   = TermName(c.freshName("_kebsCodecVar"))
    val codecProxy = TermName(c.freshName("_kebsCodecProxy"))

    val innerCall = c.untypecheck(
      q"""
      {
        var $codecVar: _root_.com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec[$tpe] =
          null.asInstanceOf[_root_.com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec[$tpe]]
        implicit val $codecProxy: _root_.com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec[$tpe] =
          new _root_.com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec[$tpe] {
            override def decodeValue(
              in: _root_.com.github.plokhotnyuk.jsoniter_scala.core.JsonReader,
              default: $tpe
            ): $tpe = $codecVar.decodeValue(in, default)
            override def encodeValue(
              x: $tpe,
              out: _root_.com.github.plokhotnyuk.jsoniter_scala.core.JsonWriter
            ): Unit = $codecVar.encodeValue(x, out)
            override def nullValue: $tpe = if ($codecVar != null) $codecVar.nullValue else null.asInstanceOf[$tpe]
          }
        $codecVar = _root_.com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker.make[$tpe]($configTree)
        $codecProxy
      }
    """
    )

    c.Expr[JsonValueCodec[A]](innerCall)
  }

  def exportCodec[A: c.WeakTypeTag](c: blackbox.Context): c.Expr[JsonValueCodec[A]] = {
    import c.universe._
    exportCodecWithConfig[A](c)(
      q"""_root_.com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
            .withAllowRecursiveTypes(true)
            .withTransientEmpty(false)
            .withTransientNone(false)"""
    )
  }

  def exportSnakifiedCodec[A: c.WeakTypeTag](c: blackbox.Context): c.Expr[JsonValueCodec[A]] = {
    import c.universe._
    exportCodecWithConfig[A](c)(
      q"""_root_.com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
            .withAllowRecursiveTypes(true)
            .withTransientEmpty(false)
            .withTransientNone(false)
            .withAdtLeafClassNameMapper(x => _root_.com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker.enforce_snake_case(_root_.com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker.simpleClassName(x)))
            .withFieldNameMapper(_root_.com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker.enforce_snake_case)"""
    )
  }

  def exportCapitalizedCodec[A: c.WeakTypeTag](c: blackbox.Context): c.Expr[JsonValueCodec[A]] = {
    import c.universe._
    exportCodecWithConfig[A](c)(
      q"""_root_.com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
            .withAllowRecursiveTypes(true)
            .withTransientEmpty(false)
            .withTransientNone(false)
            .withFieldNameMapper(_root_.com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker.EnforcePascalCase)"""
    )
  }

  def flatCodecImpl[T: c.WeakTypeTag, A: c.WeakTypeTag](c: blackbox.Context)(
      rep: c.Expr[ValueClassLike[T, A]]
  ): c.Expr[JsonValueCodec[T]] = {
    import c.universe._
    val tpe  = weakTypeOf[A]
    val tpeT = weakTypeOf[T]
    c.Expr[JsonValueCodec[T]](
      c.untypecheck(q"""
        new _root_.pl.iterators.kebs.jsoniter.KebsValueClassCodec[$tpeT, $tpe](
          $rep,
          _root_.com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker.make[$tpe](
            _root_.com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
              .withAllowRecursiveTypes(true)
              .withTransientEmpty(false)
              .withTransientNone(false)
          )
        )
      """)
    )
  }

  def instanceConverterCodecImpl[T: c.WeakTypeTag, A: c.WeakTypeTag](c: blackbox.Context)(
      rep: c.Expr[InstanceConverter[T, A]]
  ): c.Expr[JsonValueCodec[T]] = {
    import c.universe._
    val tpe  = weakTypeOf[A]
    val tpeT = weakTypeOf[T]
    c.Expr[JsonValueCodec[T]](
      c.untypecheck(q"""
        new _root_.pl.iterators.kebs.jsoniter.KebsInstanceConverterCodec[$tpeT, $tpe](
          $rep,
          _root_.com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker.make[$tpe](
            _root_.com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
              .withAllowRecursiveTypes(true)
              .withTransientEmpty(false)
              .withTransientNone(false)
          )
        )
      """)
    )
  }
}

private[jsoniter] trait KebsJsoniterFlatCodecFallback {
  implicit def deriveCodec[A]: JsonValueCodec[A] = macro KebsJsoniterMacros.exportCodec[A]
}

private[jsoniter] trait KebsJsoniterFlatCodec extends KebsJsoniterFlatCodecFallback {

  // format: off
  implicit def flatCodec[T, A](implicit rep: ValueClassLike[T, A]): JsonValueCodec[T] = macro KebsJsoniterMacros.flatCodecImpl[T, A]
  implicit def instanceConverterCodec[T, A](implicit rep: InstanceConverter[T, A]): JsonValueCodec[T] = macro KebsJsoniterMacros.instanceConverterCodecImpl[T, A]
  // format: on
}

private[jsoniter] trait KebsJsoniterSnakifiedFlatCodecFallback {
  implicit def deriveCodec[A]: JsonValueCodec[A] = macro KebsJsoniterMacros.exportSnakifiedCodec[A]
}

private[jsoniter] trait KebsJsoniterSnakifiedFlatCodec extends KebsJsoniterSnakifiedFlatCodecFallback {

  // format: off
  implicit def flatCodec[T, A](implicit rep: ValueClassLike[T, A]): JsonValueCodec[T] = macro KebsJsoniterMacros.flatCodecImpl[T, A]
  implicit def instanceConverterCodec[T, A](implicit rep: InstanceConverter[T, A]): JsonValueCodec[T] = macro KebsJsoniterMacros.instanceConverterCodecImpl[T, A]
  // format: on
}

private[jsoniter] trait KebsJsoniterCapitalizedFlatCodecFallback {
  implicit def deriveCodec[A]: JsonValueCodec[A] = macro KebsJsoniterMacros.exportCapitalizedCodec[A]
}

private[jsoniter] trait KebsJsoniterCapitalizedFlatCodec extends KebsJsoniterCapitalizedFlatCodecFallback {

  // format: off
  implicit def flatCodec[T, A](implicit rep: ValueClassLike[T, A]): JsonValueCodec[T] = macro KebsJsoniterMacros.flatCodecImpl[T, A]
  implicit def instanceConverterCodec[T, A](implicit rep: InstanceConverter[T, A]): JsonValueCodec[T] = macro KebsJsoniterMacros.instanceConverterCodecImpl[T, A]
  // format: on
}

trait KebsJsoniter            extends KebsJsoniterFlatCodec
trait KebsJsoniterSnakified   extends KebsJsoniterSnakifiedFlatCodec
trait KebsJsoniterCapitalized extends KebsJsoniterCapitalizedFlatCodec
