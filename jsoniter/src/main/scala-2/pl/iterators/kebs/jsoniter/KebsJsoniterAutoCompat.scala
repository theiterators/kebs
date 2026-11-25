package pl.iterators.kebs.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.JsonDerivationMacrosScala2
import pl.iterators.kebs.jsoniter.internal.JsoniterMacrosImpl

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[jsoniter] trait KebsJsoniterAutoCompat { self: KebsJsoniter =>
  def deriveCodec[T]: JsonValueCodec[T] = macro KebsJsoniterMacros.explicitly[T]
}

final private[jsoniter] class KebsJsoniterMacros(val c: whitebox.Context)
    extends hearth.MacroCommonsScala2
    with JsonDerivationMacrosScala2
    with JsoniterMacrosImpl {
  import c.universe.{Type => UType, _}

  private def config: Expr[JsonConfig] = c.Expr[JsonConfig](q"${c.prefix}.kebsJsonConfig")

  def explicitly[T: c.WeakTypeTag]: c.Expr[JsonValueCodec[T]] = explicitEntry {
    val enclosing = enclosingImplicit(weakTypeOf[JsonValueCodec[T]])
    val needed    = neededCodecs[T](config, preferExisting = enclosing.isEmpty, hidesExisting = enclosing.nonEmpty)
    block[T](ownSearches = 0, needed, enclosing)
  }

  private def block[T: c.WeakTypeTag](ownSearches: Int, needed: => Needed, rootName: Option[TermName]): c.Expr[JsonValueCodec[T]] =
    c.Expr[JsonValueCodec[T]](unlessInsideSearch(ownSearches, weakTypeOf[JsonValueCodec[T]]) {
      localInstances(config.tree, needed, rootName) { (_, config, tpe) =>
        (
          tq"_root_.com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec[$tpe]",
          q"_root_.pl.iterators.kebs.jsoniter.internal.JsoniterLocalCodecs.level[$tpe]($config)"
        )
      }(None)
    })

  def level[T: c.WeakTypeTag](config: c.Expr[JsonConfig]): c.Expr[JsonValueCodec[T]] = retyped(codecLevel[T](config))
}
