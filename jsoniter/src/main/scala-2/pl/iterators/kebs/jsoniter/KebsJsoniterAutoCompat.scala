package pl.iterators.kebs.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, JsonDerivationMacrosScala2}
import pl.iterators.kebs.jsoniter.internal.JsoniterMacrosImpl

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[jsoniter] trait KebsJsoniterAutoCompat { self: KebsJsoniterSemiauto =>
  implicit def kebsAutoJsonValueCodec[T]: JsonValueCodec[T] = macro KebsJsoniterMacros.auto[T]
}

// always derives the type itself, with the protocol's configuration
private[jsoniter] trait KebsJsoniterDeriveCompat { self: KebsJsoniterSemiauto =>
  def deriveCodec[T]: JsonValueCodec[T] = macro KebsJsoniterMacros.explicitly[T]
}

final private[jsoniter] class KebsJsoniterMacros(val c: whitebox.Context)
    extends hearth.MacroCommonsScala2
    with JsonDerivationMacrosScala2
    with JsoniterMacrosImpl {
  import c.universe.{Type => UType, _}

  private def config: Expr[JsonConfig] = c.Expr[JsonConfig](q"${c.prefix}.kebsJsonConfig")

  def auto[T: c.WeakTypeTag]: c.Expr[JsonValueCodec[T]] = implicitEntry {
    entryRole[T](config) match {
      case EntryPointRole.Record                            => recordedCodec[T](config)
      case EntryPointRole.Derive if !isInsideJsonCodecMaker =>
        block[T](ownSearches = 1, neededCodecs[T](config, preferExisting = false), rootName = None)(None)
      case EntryPointRole.UseExisting if !isInsideJsonCodecMaker =>
        val existing = q"_root_.pl.iterators.kebs.jsoniter.internal.JsoniterLocalCodecs.existing[${weakTypeOf[T]}]"
        block[T](ownSearches = 1, neededBesideExisting[T](config), rootName = None)(Some(existing))
      case EntryPointRole.UseExisting                         => insideJsonCodecMaker[T]
      case EntryPointRole.Derive if !isOfferedStandardType[T] => insideJsonCodecMaker[T]
      case _                                                  => c.abort(c.enclosingPosition, s"${weakTypeOf[T]} is not derived here")
    }
  }

  // it uses what it finds several times, and the codec would be a class each time
  private def isInsideJsonCodecMaker: Boolean =
    c.enclosingMacros.exists(_.macroApplication.symbol.fullName.startsWith("com.github.plokhotnyuk.jsoniter_scala.macros."))

  private def insideJsonCodecMaker[T: c.WeakTypeTag]: c.Expr[JsonValueCodec[T]] =
    c.Expr[JsonValueCodec[T]](q"_root_.pl.iterators.kebs.jsoniter.internal.JsoniterLocalCodecs.insideJsonCodecMaker[${weakTypeOf[T]}]")

  def explicitly[T: c.WeakTypeTag]: c.Expr[JsonValueCodec[T]] = explicitEntry {
    val enclosing = enclosingImplicit(weakTypeOf[JsonValueCodec[T]])
    val needed    = neededCodecs[T](config, preferExisting = enclosing.isEmpty, hidesExisting = enclosing.nonEmpty)
    block[T](ownSearches = 0, needed, enclosing)(None)
  }

  private def block[T: c.WeakTypeTag](ownSearches: Int, needed: => Needed, rootName: Option[TermName])(
      result: Option[Tree]
  ): c.Expr[JsonValueCodec[T]] =
    c.Expr[JsonValueCodec[T]](unlessInsideSearch(ownSearches, weakTypeOf[JsonValueCodec[T]]) {
      localInstances(config.tree, needed, rootName) { (_, config, tpe) =>
        (
          tq"_root_.com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec[$tpe]",
          q"_root_.pl.iterators.kebs.jsoniter.internal.JsoniterLocalCodecs.level[$tpe]($config)"
        )
      }(result)
    })

  override protected def ownLevel(instance: LocalInstance, config: Tree): Option[Tree] = {
    import instance.tpe.Underlying as X
    Some(c.untypecheck(codecLevel[X](c.Expr[JsonConfig](config)).tree))
  }

  def level[T: c.WeakTypeTag](config: c.Expr[JsonConfig]): c.Expr[JsonValueCodec[T]] = retyped(codecLevel[T](config))
  def existing[T: c.WeakTypeTag]: c.Expr[JsonValueCodec[T]]                          = explicitEntry(retyped(existingCodec[T]))
}
