package pl.iterators.kebs.sprayjson

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, FreshQuotes, JsonDerivationMacrosScala3, LocalInstances}
import pl.iterators.kebs.sprayjson.internal.SprayFormatMacrosImpl
import spray.json.RootJsonFormat

import scala.deriving.Mirror
import scala.quoted.*

// a parent of KebsSprayJson, so that jsonFlatFormat wins a JsonFormat request for a value class (as in kebs 2)
private[sprayjson] trait KebsSprayJsonAutoCompat { self: KebsSprayJsonSemiauto =>
  inline implicit def kebsAutoJsonFormat[T](using m: Mirror.ProductOf[T]): RootJsonFormat[T] =
    KebsSprayJsonMacros.auto[T](kebsJsonConfig)

  // transparent and stepping aside when a format exists: a sealed enum also has kebs' enum format, which must win
  transparent inline implicit def kebsAutoJsonFormatSealed[T](using m: Mirror.SumOf[T]): RootJsonFormat[T] =
    KebsSprayJsonMacros.autoSealed[T](kebsJsonConfig)

}

// always derives the type itself, with the protocol's configuration
private[sprayjson] trait KebsSprayJsonDeriveCompat { self: KebsSprayJsonSemiauto =>
  inline def deriveFormat[T]: RootJsonFormat[T] = KebsSprayJsonMacros.explicitly[T](kebsJsonConfig)

  @deprecated("use deriveFormat", "3.0.0")
  inline def jsonFormatN[T]: RootJsonFormat[T] = KebsSprayJsonMacros.explicitly[T](kebsJsonConfig)
  @deprecated("use deriveFormat", "3.0.0")
  inline def jsonFormatSealed[T]: RootJsonFormat[T] = KebsSprayJsonMacros.explicitly[T](kebsJsonConfig)
  @deprecated("use deriveFormat, which handles recursive types", "3.0.0")
  inline final def jsonFormatRec[T]: RootJsonFormat[T] = KebsSprayJsonMacros.explicitly[T](kebsJsonConfig)
}

final private[sprayjson] class KebsSprayJsonMacros(q: Quotes)
    extends hearth.MacroCommonsScala3(using q),
      JsonDerivationMacrosScala3,
      SprayFormatMacrosImpl {

  def auto[A](body: => A): A       = implicitEntry(body)
  def explicitly[A](body: => A): A = explicitEntry(body)

  def needed[T: Type](config: Expr[JsonConfig], preferExisting: Boolean): LocalInstances.Needed =
    untyped(neededFormats[T](config, preferExisting))
  def neededBeside[T: Type](config: Expr[JsonConfig]): LocalInstances.Needed = untyped(neededBesideExisting[T](config))
}

private[sprayjson] object KebsSprayJsonMacros {
  inline def auto[T](inline config: JsonConfig): RootJsonFormat[T]                   = ${ autoImpl[T]('config) }
  transparent inline def autoSealed[T](inline config: JsonConfig): RootJsonFormat[T] = ${ autoSealedImpl[T]('config) }
  inline def explicitly[T](inline config: JsonConfig): RootJsonFormat[T]             = ${ explicitlyImpl[T]('config) }
  inline def derived[T](inline config: JsonConfig): RootJsonFormat[T]                = ${ derivedImpl[T]('config) }
  inline def level[T](inline config: JsonConfig): RootJsonFormat[T]                  = ${ levelImpl[T]('config) }
  inline def existing[T]: RootJsonFormat[T]                                          = ${ existingImpl[T] }

  private def macros(using q: Quotes): KebsSprayJsonMacros = new KebsSprayJsonMacros(FreshQuotes(q))

  def autoImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[RootJsonFormat[T]] = {
    val m = macros
    m.auto {
      m.formatRole[T](stepsAsideForExisting = false) match {
        case EntryPointRole.StepAside   => stepAside[T]
        case EntryPointRole.Record      => m.recordedFormat[T](config)
        case EntryPointRole.Derive      => block[T](config, m.needed[T](config, preferExisting = false))(None)
        case EntryPointRole.UseExisting => block[T](config, m.neededBeside[T](config))(Some('{ KebsSprayJsonMacros.existing[T] }))
      }
    }
  }

  // spray formats the standard library itself; failing here only skips this candidate
  def autoSealedImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[RootJsonFormat[T]] = {
    val m = macros
    m.auto {
      if (!m.isDerived[T](config)) stepAside[T]
      else
        m.formatRole[T](stepsAsideForExisting = true) match {
          case EntryPointRole.Record => m.recordedFormat[T](config)
          // not transparent, so expanded after typing: here the search for this very format is still open
          case EntryPointRole.Derive => '{ KebsSprayJsonMacros.derived[T]($config) }
          case _                     => stepAside[T]
        }
    }
  }

  private def stepAside[T: Type](using Quotes): Nothing =
    quotes.reflect.report.errorAndAbort(s"${Type.show[T]} is not derived here")

  def derivedImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[RootJsonFormat[T]] = {
    val m = macros
    m.auto(block[T](config, m.needed[T](config, preferExisting = false))(None))
  }

  // recursive uses go through the implicit being defined (implicit val format = jsonFormatN[T]), if there is one
  def explicitlyImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[RootJsonFormat[T]] = {
    val m = macros
    m.explicitly(block[T](config, m.needed[T](config, preferExisting = true))(None))
  }

  private def block[T: Type](config: Expr[JsonConfig], needed: LocalInstances.Needed)(
      result: Option[Expr[RootJsonFormat[T]]]
  )(using Quotes): Expr[RootJsonFormat[T]] =
    LocalInstances.block[RootJsonFormat[T]](config, needed)(
      new LocalInstances.Define {
        def apply[X: Type](instance: LocalInstances.Untyped, config: Expr[JsonConfig])(using Quotes): LocalInstances.Definition[?] =
          new LocalInstances.Definition('{ KebsSprayJsonMacros.level[X]($config) })
      }
    )(result)

  def levelImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[RootJsonFormat[T]] = macros.formatLevel[T](config)

  def existingImpl[T: Type](using q: Quotes): Expr[RootJsonFormat[T]] = {
    val m = macros
    m.explicitly(m.existingFormat[T])
  }
}
