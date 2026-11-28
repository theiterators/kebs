package pl.iterators.kebs.circe

import io.circe.`export`.Exported
import io.circe.{Codec, Decoder, Encoder}
import pl.iterators.kebs.circe.internal.CirceMacrosImpl
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, JsonDerivationMacrosScala2}

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

// Exported: circe's low-priority hook, so instances in a type's companion win over derivation
private[circe] trait KebsCirceAutoCompat { this: KebsCirceSemiauto =>
  implicit def kebsAutoEncoder[A]: Exported[Encoder.AsObject[A]] = macro KebsCirceMacros.autoEncoder[A]
  implicit def kebsAutoDecoder[A]: Exported[Decoder[A]] = macro KebsCirceMacros.autoDecoder[A]
}

// always derive the type itself, with the protocol's configuration
private[circe] trait KebsCirceDeriveCompat { this: KebsCirceSemiauto =>
  def deriveEncoder[A]: Encoder.AsObject[A] = macro KebsCirceMacros.explicitEncoder[A]
  def deriveDecoder[A]: Decoder[A] = macro KebsCirceMacros.explicitDecoder[A]
  def deriveCodec[A]: Codec.AsObject[A] = macro KebsCirceMacros.explicitCodec[A]
}

final private[circe] class KebsCirceMacros(val c: whitebox.Context)
    extends hearth.MacroCommonsScala2
    with JsonDerivationMacrosScala2
    with CirceMacrosImpl {
  import c.universe.{Type => UType, _}

  // kebs 2 derived them on Scala 2
  override protected def derivesPekkoHierarchies: Boolean = true

  private def config: Expr[JsonConfig] = c.Expr[JsonConfig](q"${c.prefix}.kebsJsonConfig")

  private def requireDerivable(A: UType): Unit = {
    val symbol = A.typeSymbol
    if (!(symbol.isClass && (symbol.asClass.isCaseClass || symbol.asClass.isSealed))) stepAside(A)
  }

  private def stepAside(A: UType): Nothing = c.abort(c.enclosingPosition, s"$A is not derived here")

  private def exported(codec: Tree): Tree = q"_root_.io.circe.`export`.Exported($codec)"

  def autoEncoder[A: c.WeakTypeTag]: Tree = implicitEntry {
    requireDerivable(weakTypeOf[A])
    encoderRole[A](config) match {
      case EntryPointRole.Record => exported(recordedEncoder[A](config).tree)
      case EntryPointRole.Derive => exported(block(weakTypeOf[Encoder.AsObject[A]])(neededEncoders[A](config)))
      case _                     => stepAside(weakTypeOf[A])
    }
  }

  def autoDecoder[A: c.WeakTypeTag]: Tree = implicitEntry {
    requireDerivable(weakTypeOf[A])
    decoderRole[A](config) match {
      case EntryPointRole.Record => exported(recordedDecoder[A](config).tree)
      case EntryPointRole.Derive => exported(block(weakTypeOf[Decoder[A]])(neededDecoders[A](config)))
      case _                     => stepAside(weakTypeOf[A])
    }
  }

  def explicitEncoder[A: c.WeakTypeTag]: Tree = explicitEntry {
    requireDerivable(weakTypeOf[A])
    val enclosing = enclosingImplicit(weakTypeOf[Encoder[A]])
    explicitBlock(weakTypeOf[Encoder.AsObject[A]], enclosing)(
      neededEncoders[A](config, preferExisting = enclosing.isEmpty, hidesExisting = enclosing.nonEmpty)
    )
  }

  def explicitDecoder[A: c.WeakTypeTag]: Tree = explicitEntry {
    requireDerivable(weakTypeOf[A])
    val enclosing = enclosingImplicit(weakTypeOf[Decoder[A]])
    explicitBlock(weakTypeOf[Decoder[A]], enclosing)(
      neededDecoders[A](config, preferExisting = enclosing.isEmpty, hidesExisting = enclosing.nonEmpty)
    )
  }

  def explicitCodec[A: c.WeakTypeTag]: Tree = {
    val A = weakTypeOf[A]
    q"_root_.io.circe.Codec.AsObject.from(${c.prefix}.deriveDecoder[$A], ${c.prefix}.deriveEncoder[$A])"
  }

  private def explicitBlock(result: UType, rootName: Option[TermName])(needed: => Needed): Tree =
    unlessInsideSearch(ownSearches = 0, result)(codecBlock(needed, rootName))

  // its own searches: for the Exported instance and for the type class it stands for
  private def block(result: UType)(needed: => Needed): Tree =
    unlessInsideSearch(ownSearches = 2, result)(codecBlock(needed, rootName = None))

  private def codecBlock(needed: Needed, rootName: Option[TermName]): Tree = {
    val codecs = q"_root_.pl.iterators.kebs.circe.internal.CirceLocalCodecs"
    localInstances(config.tree, needed, rootName) { (instance, config, tpe) =>
      if (instance.typeClass == "Encoder") (tq"_root_.io.circe.Encoder.AsObject[$tpe]", q"$codecs.encoder[$tpe]($config)")
      else (tq"_root_.io.circe.Decoder[$tpe]", q"$codecs.decoder[$tpe]($config)")
    }(None)
  }

  override protected def ownLevel(instance: LocalInstance, config: Tree): Option[Tree] = {
    import instance.tpe.Underlying as X
    val configExpr = c.Expr[JsonConfig](config)
    Some(c.untypecheck((if (instance.typeClass == "Encoder") encoderLevel[X](configExpr) else decoderLevel[X](configExpr)).tree))
  }

  def oneEncoder[T: c.WeakTypeTag](config: c.Expr[JsonConfig]): c.Expr[Encoder.AsObject[T]] = retyped(encoderLevel[T](config))
  def oneDecoder[T: c.WeakTypeTag](config: c.Expr[JsonConfig]): c.Expr[Decoder[T]]          = retyped(decoderLevel[T](config))
}
