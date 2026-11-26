package pl.iterators.kebs.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, FreshQuotes, JsonDerivationMacrosScala3, LocalInstances}
import pl.iterators.kebs.jsoniter.internal.JsoniterMacrosImpl

import scala.quoted.*

private[jsoniter] trait KebsJsoniterAutoCompat { self: KebsJsoniter =>
  // transparent: where nothing is derived, the search goes on
  transparent inline implicit def kebsAutoJsonValueCodec[T]: JsonValueCodec[T] = KebsJsoniterMacros.auto[T](kebsJsonConfig)

  inline def deriveCodec[T]: JsonValueCodec[T] = KebsJsoniterMacros.explicitly[T](kebsJsonConfig)
}

final private[jsoniter] class KebsJsoniterMacros(q: Quotes)
    extends hearth.MacroCommonsScala3(using q),
      JsonDerivationMacrosScala3,
      JsoniterMacrosImpl {

  def auto[A](body: => A): A       = implicitEntry(body)
  def explicitly[A](body: => A): A = explicitEntry(body)

  def needed[T: Type](config: Expr[JsonConfig], preferExisting: Boolean): LocalInstances.Needed =
    untyped(neededCodecs[T](config, preferExisting))
  def neededBeside[T: Type](config: Expr[JsonConfig]): LocalInstances.Needed = untyped(neededBesideExisting[T](config))
}

private[jsoniter] object KebsJsoniterMacros {
  transparent inline def auto[T](inline config: JsonConfig): JsonValueCodec[T] = ${ autoImpl[T]('config) }
  inline def derived[T](inline config: JsonConfig): JsonValueCodec[T]          = ${ derivedImpl[T]('config) }
  inline def usingExisting[T](inline config: JsonConfig): JsonValueCodec[T]    = ${ usingExistingImpl[T]('config) }
  inline def existing[T]: JsonValueCodec[T]                                    = ${ existingImpl[T] }
  inline def explicitly[T](inline config: JsonConfig): JsonValueCodec[T]       = ${ explicitlyImpl[T]('config) }
  inline def level[T](inline config: JsonConfig): JsonValueCodec[T]            = ${ levelImpl[T]('config) }

  private def macros(using q: Quotes): KebsJsoniterMacros = new KebsJsoniterMacros(FreshQuotes(q))

  def autoImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[JsonValueCodec[T]] = {
    val m = macros
    m.auto {
      m.entryRole[T](config) match {
        case EntryPointRole.Record => m.recordedCodec[T](config)
        // not transparent, so expanded after typing: here the search for this very codec is still open
        case EntryPointRole.Derive      => '{ KebsJsoniterMacros.derived[T]($config) }
        case EntryPointRole.UseExisting => '{ KebsJsoniterMacros.usingExisting[T]($config) }
        case _                          => quotes.reflect.report.errorAndAbort(s"${Type.show[T]} is not derived here")
      }
    }
  }

  def derivedImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[JsonValueCodec[T]] = {
    val m = macros
    m.auto(block[T](config, m.needed[T](config, preferExisting = false))(None))
  }

  def usingExistingImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[JsonValueCodec[T]] = {
    val m = macros
    m.auto(block[T](config, m.neededBeside[T](config))(Some('{ KebsJsoniterMacros.existing[T] })))
  }

  def existingImpl[T: Type](using q: Quotes): Expr[JsonValueCodec[T]] = {
    val m = macros
    m.explicitly(m.existingCodec[T])
  }

  // recursive uses go through the implicit being defined (implicit val codec = deriveCodec[T]), if there is one
  def explicitlyImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[JsonValueCodec[T]] = {
    val m = macros
    m.explicitly(block[T](config, m.needed[T](config, preferExisting = true))(None))
  }

  private def block[T: Type](config: Expr[JsonConfig], needed: LocalInstances.Needed)(
      result: Option[Expr[JsonValueCodec[T]]]
  )(using Quotes): Expr[JsonValueCodec[T]] =
    LocalInstances.block[JsonValueCodec[T]](config, needed)(
      new LocalInstances.Define {
        def apply[X: Type](instance: LocalInstances.Untyped, config: Expr[JsonConfig])(using Quotes): LocalInstances.Definition[?] =
          new LocalInstances.Definition('{ KebsJsoniterMacros.level[X]($config) })
      }
    )(result)

  def levelImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[JsonValueCodec[T]] = macros.codecLevel[T](config)
}
