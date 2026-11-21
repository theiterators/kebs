package pl.iterators.kebs.playjson

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.core.macros.internal.JsonDerivationMacrosScala3
import pl.iterators.kebs.playjson.internal.PlayJsonMacrosImpl
import play.api.libs.json.{OFormat, OWrites, Reads}

import scala.deriving.Mirror
import scala.util.NotGiven
import scala.quoted.*

// a parent of KebsPlayJson, so that its flat and InstanceConverter instances win by owner
private[playjson] trait KebsPlayJsonAutoCompat { self: KebsPlayJson =>
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
    KebsPlayJsonMacros.autoWritesSealed[T](kebsJsonConfig)

  inline def deriveReads[T]: Reads[T]    = KebsPlayJsonMacros.explicitReads[T](kebsJsonConfig)
  inline def deriveWrites[T]: OWrites[T] = KebsPlayJsonMacros.explicitWrites[T](kebsJsonConfig)
  inline def deriveFormat[T]: OFormat[T] = KebsPlayJsonMacros.explicitFormat[T](kebsJsonConfig)
}

final private[playjson] class KebsPlayJsonMacros(q: Quotes)
    extends hearth.MacroCommonsScala3(using q),
      PlayJsonMacrosImpl,
      JsonDerivationMacrosScala3 {

  // play formats the standard library itself; failing here only skips the (transparent) candidate
  def skipStandardLibrary[T: Type](): Unit =
    if (isStandardLibrary[T]) Environment.reportErrorAndAbort(s"${Type[T].prettyPrint} is not derived here")
}

private[playjson] object KebsPlayJsonMacros {
  inline def autoReads[T](inline config: JsonConfig): Reads[T]                      = ${ autoReadsImpl[T]('config) }
  transparent inline def autoWrites[T](inline config: JsonConfig): OWrites[T]       = ${ autoWritesImpl[T]('config) }
  transparent inline def autoReadsSealed[T](inline config: JsonConfig): Reads[T]    = ${ autoReadsSealedImpl[T]('config) }
  transparent inline def autoWritesSealed[T](inline config: JsonConfig): OWrites[T] = ${ autoWritesSealedImpl[T]('config) }
  inline def explicitReads[T](inline config: JsonConfig): Reads[T]                  = ${ explicitReadsImpl[T]('config) }
  inline def explicitWrites[T](inline config: JsonConfig): OWrites[T]               = ${ explicitWritesImpl[T]('config) }
  inline def explicitFormat[T](inline config: JsonConfig): OFormat[T]               = ${ explicitFormatImpl[T]('config) }

  def autoReadsImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Reads[T]] =
    new KebsPlayJsonMacros(q).deriveReadsRoot[T](config, consultExisting = true, stepAside = false)

  def autoWritesImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[OWrites[T]] = {
    val macros = new KebsPlayJsonMacros(q)
    macros.skipStandardLibrary[T]()
    macros.deriveOWrites[T](config, consultExisting = true, stepAside = true)
  }

  def autoReadsSealedImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Reads[T]] = {
    val macros = new KebsPlayJsonMacros(q)
    macros.skipStandardLibrary[T]()
    macros.deriveReadsRoot[T](config, consultExisting = true, stepAside = true)
  }

  def autoWritesSealedImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[OWrites[T]] = {
    val macros = new KebsPlayJsonMacros(q)
    macros.skipStandardLibrary[T]()
    macros.deriveOWrites[T](config, consultExisting = true, stepAside = true)
  }

  def explicitReadsImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[Reads[T]] =
    new KebsPlayJsonMacros(q).deriveReadsRoot[T](config, consultExisting = false, stepAside = false)

  def explicitWritesImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[OWrites[T]] =
    new KebsPlayJsonMacros(q).deriveOWrites[T](config, consultExisting = false, stepAside = false)

  def explicitFormatImpl[T: Type](config: Expr[JsonConfig])(using q: Quotes): Expr[OFormat[T]] =
    new KebsPlayJsonMacros(q).deriveOFormat[T](config)
}
