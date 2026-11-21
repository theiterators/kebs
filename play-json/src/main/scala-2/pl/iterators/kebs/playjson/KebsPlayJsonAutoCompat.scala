package pl.iterators.kebs.playjson

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.playjson.internal.PlayJsonMacrosImpl
import play.api.libs.json.{OFormat, OWrites, Reads}

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

// a parent of KebsPlayJson, so that its flat and InstanceConverter instances win by owner
private[playjson] trait KebsPlayJsonAutoCompat { self: KebsPlayJson =>
  implicit def kebsAutoReads[T]: Reads[T] = macro KebsPlayJsonMacros.autoReads[T]
  implicit def kebsAutoReadsSealed[T]: Reads[T] = macro KebsPlayJsonMacros.autoReadsSealed[T]
  implicit def kebsAutoWrites[T]: OWrites[T] = macro KebsPlayJsonMacros.autoWrites[T]
  implicit def kebsAutoWritesSealed[T]: OWrites[T] = macro KebsPlayJsonMacros.autoWritesSealed[T]

  def deriveReads[T]: Reads[T] = macro KebsPlayJsonMacros.explicitReads[T]
  def deriveWrites[T]: OWrites[T] = macro KebsPlayJsonMacros.explicitWrites[T]
  def deriveFormat[T]: OFormat[T] = macro KebsPlayJsonMacros.explicitFormat[T]
}

final private[playjson] class KebsPlayJsonMacros(val c: whitebox.Context) extends hearth.MacroCommonsScala2 with PlayJsonMacrosImpl {
  import c.universe.{Type => UType, _}

  private def isCaseClass(T: UType): Boolean = T.typeSymbol.isClass && T.typeSymbol.asClass.isCaseClass
  private def isSealed(T: UType): Boolean    = T.typeSymbol.isClass && T.typeSymbol.asClass.isSealed && !isCaseClass(T)

  private def config: Expr[JsonConfig] = c.Expr[JsonConfig](q"${c.prefix}.kebsJsonConfig")

  // the implicit entries step aside when an instance exists: a user's Reads[X] is neither more nor less specific than
  // kebsAutoReads[X]; play formats the standard library itself; a type already being derived is the enclosing derivation's
  private def implicitEntry[T: c.WeakTypeTag](sealedType: Boolean): Unit = {
    val T = weakTypeOf[T]
    if (sealedType && !isSealed(T)) c.abort(c.enclosingPosition, s"$T is not a sealed hierarchy")
    if (!sealedType && !isCaseClass(T)) c.abort(c.enclosingPosition, s"$T is not a case class")
    if (isStandardLibrary[T] || isBeingDerived[T]) c.abort(c.enclosingPosition, s"$T is not derived here")
  }

  private def explicitEntry[T: c.WeakTypeTag](): Unit = {
    val T = weakTypeOf[T]
    if (!isCaseClass(T) && !isSealed(T)) c.abort(c.enclosingPosition, s"$T is not a case class or a sealed hierarchy")
  }

  // re-typed, so that the owners of the local codecs are fixed before lambdalift sees the recursive references
  private def retyped[A](expr: Expr[A]): c.Expr[A] = c.Expr[A](c.untypecheck(expr.tree))

  def autoReads[T: c.WeakTypeTag]: c.Expr[Reads[T]] = {
    implicitEntry[T](sealedType = false)
    retyped(deriveReadsRoot[T](config, consultExisting = true, stepAside = true))
  }

  def autoReadsSealed[T: c.WeakTypeTag]: c.Expr[Reads[T]] = {
    implicitEntry[T](sealedType = true)
    retyped(deriveReadsRoot[T](config, consultExisting = true, stepAside = true))
  }

  def autoWrites[T: c.WeakTypeTag]: c.Expr[OWrites[T]] = {
    implicitEntry[T](sealedType = false)
    retyped(deriveOWrites[T](config, consultExisting = true, stepAside = true))
  }

  def autoWritesSealed[T: c.WeakTypeTag]: c.Expr[OWrites[T]] = {
    implicitEntry[T](sealedType = true)
    retyped(deriveOWrites[T](config, consultExisting = true, stepAside = true))
  }

  def explicitReads[T: c.WeakTypeTag]: c.Expr[Reads[T]] = {
    explicitEntry[T]()
    retyped(deriveReadsRoot[T](config, consultExisting = false, stepAside = false))
  }

  def explicitWrites[T: c.WeakTypeTag]: c.Expr[OWrites[T]] = {
    explicitEntry[T]()
    retyped(deriveOWrites[T](config, consultExisting = false, stepAside = false))
  }

  def explicitFormat[T: c.WeakTypeTag]: c.Expr[OFormat[T]] = {
    explicitEntry[T]()
    retyped(deriveOFormat[T](config))
  }
}
