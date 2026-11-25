package pl.iterators.kebs.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{FreshQuotes, JsonDerivationMacrosScala3, LocalInstances}
import pl.iterators.kebs.jsoniter.internal.JsoniterMacrosImpl

import scala.quoted.*

private[jsoniter] trait KebsJsoniterAutoCompat { self: KebsJsoniter =>
  inline def deriveCodec[T]: JsonValueCodec[T] = KebsJsoniterMacros.explicitly[T](kebsJsonConfig)
}

final private[jsoniter] class KebsJsoniterMacros(q: Quotes)
    extends hearth.MacroCommonsScala3(using q),
      JsonDerivationMacrosScala3,
      JsoniterMacrosImpl {

  def explicitly[A](body: => A): A = explicitEntry(body)

  def needed[T: Type](config: Expr[JsonConfig], preferExisting: Boolean): LocalInstances.Needed =
    untyped(neededCodecs[T](config, preferExisting))
}

private[jsoniter] object KebsJsoniterMacros {
  inline def explicitly[T](inline config: JsonConfig): JsonValueCodec[T] = ${ explicitlyImpl[T]('config) }
  inline def level[T](inline config: JsonConfig): JsonValueCodec[T]      = ${ levelImpl[T]('config) }

  private def macros(using q: Quotes): KebsJsoniterMacros = new KebsJsoniterMacros(FreshQuotes(q))

  // recursive uses go through the implicit being defined (implicit val codec = deriveCodec[T]), if there is one
  def explicitlyImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[JsonValueCodec[T]] = {
    val m = macros
    m.explicitly(block[T](config, m.needed[T](config, preferExisting = true)))
  }

  private def block[T: Type](config: Expr[JsonConfig], needed: LocalInstances.Needed)(using Quotes): Expr[JsonValueCodec[T]] =
    LocalInstances.block[JsonValueCodec[T]](config, needed)(
      new LocalInstances.Define {
        def apply[X: Type](instance: LocalInstances.Untyped, config: Expr[JsonConfig])(using Quotes): LocalInstances.Definition[?] =
          new LocalInstances.Definition('{ KebsJsoniterMacros.level[X]($config) })
      }
    )(None)

  def levelImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[JsonValueCodec[T]] = macros.codecLevel[T](config)
}
