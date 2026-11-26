package pl.iterators.kebs.playjson

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.{EntryPointRole, FreshQuotes, JsonDerivationMacrosScala3, LocalInstances}
import pl.iterators.kebs.playjson.internal.PlayJsonMacrosImpl
import play.api.libs.json.{OFormat, OWrites, Reads, Writes}

import scala.deriving.Mirror
import scala.quoted.*
import scala.util.NotGiven

// a parent of KebsPlayJson, so that its flat and InstanceConverter instances win by owner
private[playjson] trait KebsPlayJsonAutoCompat { self: KebsPlayJsonSemiauto =>
  // not a case object's own type: play's Reads/Writes are invariant, so kebs' enum instances do not apply there and
  // deriving `{}` would replace a compile error with wrong JSON
  inline implicit def kebsAutoReads[T](using m: Mirror.ProductOf[T], notCaseObject: NotGiven[T <:< Singleton]): Reads[T] =
    KebsPlayJsonMacros.autoReads[T](kebsJsonConfig)

  // transparent and stepping aside when an instance exists: a sealed enum also has kebs' enum instances, which must win
  transparent inline implicit def kebsAutoReadsSealed[T](using m: Mirror.SumOf[T]): Reads[T] =
    KebsPlayJsonMacros.autoReadsSealed[T](kebsJsonConfig)

  // OWrites, so that play's OFormat.oFormatFromReadsAndOWrites serves OFormat requests; transparent and stepping aside
  // when an instance exists: otherwise it would beat a companion Format and have to wrap a non-object Writes
  transparent inline implicit def kebsAutoWrites[T](using m: Mirror.ProductOf[T], notCaseObject: NotGiven[T <:< Singleton]): OWrites[T] =
    KebsPlayJsonMacros.autoWrites[T](kebsJsonConfig)
  transparent inline implicit def kebsAutoWritesSealed[T](using m: Mirror.SumOf[T]): OWrites[T] =
    KebsPlayJsonMacros.autoWrites[T](kebsJsonConfig)
}

// always derive the type itself, with the protocol's configuration
private[playjson] trait KebsPlayJsonDeriveCompat { self: KebsPlayJsonSemiauto =>
  inline def deriveReads[T]: Reads[T]    = KebsPlayJsonMacros.explicitReads[T](kebsJsonConfig)
  inline def deriveWrites[T]: OWrites[T] = KebsPlayJsonMacros.explicitWrites[T](kebsJsonConfig)
  inline def deriveFormat[T]: OFormat[T] =
    OFormat[T](KebsPlayJsonMacros.explicitReads[T](kebsJsonConfig), KebsPlayJsonMacros.explicitWrites[T](kebsJsonConfig))
}

final private[playjson] class KebsPlayJsonMacros(q: Quotes)
    extends hearth.MacroCommonsScala3(using q),
      JsonDerivationMacrosScala3,
      PlayJsonMacrosImpl {

  def auto[A](body: => A): A       = implicitEntry(body)
  def explicitly[A](body: => A): A = explicitEntry(body)

  def readsNeeded[T: Type](config: Expr[JsonConfig], preferExisting: Boolean): LocalInstances.Needed =
    untyped(neededReads[T](config, preferExisting))
  def readsNeededBesideExisting[T: Type](config: Expr[JsonConfig]): LocalInstances.Needed = untyped(neededReadsBesideExisting[T](config))
  def writesNeeded[T: Type](config: Expr[JsonConfig], preferExisting: Boolean): LocalInstances.Needed =
    untyped(neededWrites[T](config, preferExisting))
}

private[playjson] object KebsPlayJsonMacros {
  inline def autoReads[T](inline config: JsonConfig): Reads[T]                   = ${ autoReadsImpl[T]('config) }
  transparent inline def autoReadsSealed[T](inline config: JsonConfig): Reads[T] = ${ autoReadsSealedImpl[T]('config) }
  transparent inline def autoWrites[T](inline config: JsonConfig): OWrites[T]    = ${ autoWritesImpl[T]('config) }
  inline def explicitReads[T](inline config: JsonConfig): Reads[T]               = ${ explicitReadsImpl[T]('config) }
  inline def explicitWrites[T](inline config: JsonConfig): OWrites[T]            = ${ explicitWritesImpl[T]('config) }
  // not transparent, so expanded after typing: in a transparent entry the search for this very instance is still open
  inline def derivedReads[T](inline config: JsonConfig): Reads[T]         = ${ derivedReadsImpl[T]('config) }
  inline def derivedWrites[T](inline config: JsonConfig): OWrites[T]      = ${ derivedWritesImpl[T]('config) }
  inline def oneReads[T](inline config: JsonConfig): Reads[T]             = ${ oneReadsImpl[T]('config) }
  inline def oneWrites[T](inline config: JsonConfig): OWrites[T]          = ${ oneWritesImpl[T]('config) }
  inline def oneCollectionWrites[T](inline config: JsonConfig): Writes[T] = ${ oneCollectionWritesImpl[T]('config) }
  inline def existingReads[T]: Reads[T]                                   = ${ existingReadsImpl[T] }

  private def macros(using q: Quotes): KebsPlayJsonMacros = new KebsPlayJsonMacros(FreshQuotes(q))

  def autoReadsImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Reads[T]] = {
    val m = macros
    m.auto {
      m.readsRole[T](stepsAsideForExisting = false) match {
        case EntryPointRole.StepAside   => stepAside[T]
        case EntryPointRole.Record      => m.recordedReads[T](config)
        case EntryPointRole.Derive      => block[Reads[T]](config, m.readsNeeded[T](config, preferExisting = false))(None)
        case EntryPointRole.UseExisting =>
          block[Reads[T]](config, m.readsNeededBesideExisting[T](config))(Some('{ KebsPlayJsonMacros.existingReads[T] }))
      }
    }
  }

  // play formats the standard library itself; failing here only skips a transparent candidate
  def autoReadsSealedImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Reads[T]] = {
    val m = macros
    m.auto {
      if (m.isStandardLibrary[T]) stepAside[T]
      else
        m.readsRole[T](stepsAsideForExisting = true) match {
          case EntryPointRole.Record => m.recordedReads[T](config)
          case EntryPointRole.Derive => '{ KebsPlayJsonMacros.derivedReads[T]($config) }
          case _                     => stepAside[T]
        }
    }
  }

  def autoWritesImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[OWrites[T]] = {
    val m = macros
    m.auto {
      if (m.isStandardLibrary[T]) stepAside[T]
      else
        m.writesRole[T] match {
          case EntryPointRole.Record => m.recordedWrites[T](config)
          case EntryPointRole.Derive => '{ KebsPlayJsonMacros.derivedWrites[T]($config) }
          case _                     => stepAside[T]
        }
    }
  }

  private def stepAside[T: Type](using Quotes): Nothing =
    quotes.reflect.report.errorAndAbort(s"${Type.show[T]} is not derived here")

  def derivedReadsImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Reads[T]] = {
    val m = macros
    m.auto(block[Reads[T]](config, m.readsNeeded[T](config, preferExisting = false))(None))
  }

  def derivedWritesImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[OWrites[T]] = {
    val m = macros
    m.auto(block[OWrites[T]](config, m.writesNeeded[T](config, preferExisting = false))(None))
  }

  // recursive uses go through the implicit being defined (implicit val reads = deriveReads[T]), if there is one
  def explicitReadsImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Reads[T]] = {
    val m = macros
    m.explicitly(block[Reads[T]](config, m.readsNeeded[T](config, preferExisting = true))(None))
  }

  def explicitWritesImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[OWrites[T]] = {
    val m = macros
    m.explicitly(block[OWrites[T]](config, m.writesNeeded[T](config, preferExisting = true))(None))
  }

  private def block[R: Type](config: Expr[JsonConfig], needed: LocalInstances.Needed)(
      result: Option[Expr[R]]
  )(using Quotes): Expr[R] =
    LocalInstances.block[R](config, needed)(
      new LocalInstances.Define {
        def apply[X: Type](instance: LocalInstances.Untyped, config: Expr[JsonConfig])(using Quotes): LocalInstances.Definition[?] =
          if (instance.typeClass == "Reads") new LocalInstances.Definition('{ KebsPlayJsonMacros.oneReads[X]($config) })
          else if (instance.isCollection) new LocalInstances.Definition('{ KebsPlayJsonMacros.oneCollectionWrites[X]($config) })
          else new LocalInstances.Definition('{ KebsPlayJsonMacros.oneWrites[X]($config) })
      }
    )(result)

  def oneReadsImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Reads[T]] = macros.readsLevel[T](config)

  def oneWritesImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[OWrites[T]] = macros.writesLevel[T](config)

  def oneCollectionWritesImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Writes[T]] =
    macros.collectionWritesLevel[T](config)

  def existingReadsImpl[T: Type](using q: Quotes): Expr[Reads[T]] = {
    val m = macros
    m.explicitly(m.existingReads[T])
  }
}
