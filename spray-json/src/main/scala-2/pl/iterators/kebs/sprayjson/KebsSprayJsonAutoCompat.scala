package pl.iterators.kebs.sprayjson

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.sprayjson.internal.SprayFormatMacrosImpl
import spray.json.RootJsonFormat

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[sprayjson] trait KebsSprayJsonAutoCompat { self: KebsSprayJson =>
  implicit def kebsAutoJsonFormat[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.autoProduct[T]
  implicit def kebsAutoJsonFormatSealed[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.autoSealed[T]

  def jsonFormatN[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.explicitProduct[T]
  def jsonFormatSealed[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.explicitSealed[T]

  @deprecated("jsonFormatN handles recursive types", "3.0.0")
  final def jsonFormatRec[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.explicitProduct[T]
}

final private[sprayjson] class KebsSprayJsonMacros(val c: whitebox.Context) extends hearth.MacroCommonsScala2 with SprayFormatMacrosImpl {
  import c.universe.{Type => UType, _}

  private def isLookingFor(tpe: UType): Boolean = c.enclosingImplicits.headOption.exists(_.pt.typeSymbol == tpe.typeSymbol)

  // the product entry is more specific than jsonFlatFormat, so it steps aside when a JsonFormat/Reader/Writer of a value
  // class is wanted (kebs 2's rule); a RootJsonFormat request derives an object
  private def flatPreferred(T: UType): Boolean =
    (isLookingFor(typeOf[spray.json.JsonFormat[Any]]) || isLookingFor(typeOf[spray.json.JsonReader[Any]]) ||
      isLookingFor(typeOf[spray.json.JsonWriter[Any]])) && (T.decls.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }.toList match {
      case single :: Nil =>
        val valueClassLike = appliedType(
          typeOf[pl.iterators.kebs.core.macros.ValueClassLike[Any, Any]].typeConstructor,
          T,
          single.typeSignatureIn(T).finalResultType
        )
        c.inferImplicitValue(valueClassLike, silent = true) != EmptyTree
      case _ => false
    })

  private def isCaseClass(T: UType): Boolean = T.typeSymbol.isClass && T.typeSymbol.asClass.isCaseClass
  private def isSealed(T: UType): Boolean    = T.typeSymbol.isClass && T.typeSymbol.asClass.isSealed && !isCaseClass(T)

  // spray formats the standard library itself; a type already being derived further up is the enclosing derivation's
  private def skipForImplicitSearch[T: c.WeakTypeTag](): Unit =
    if (isStandardLibrary[T] || isBeingDerived[T]) c.abort(c.enclosingPosition, s"${weakTypeOf[T]} is not derived here")

  def autoProduct[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    val T = weakTypeOf[T]
    if (!isCaseClass(T)) c.abort(c.enclosingPosition, s"$T is not a case class")
    skipForImplicitSearch[T]()
    if (flatPreferred(T)) c.abort(c.enclosingPosition, "Flat format preferred")
    derive[T](consultExisting = true, stepAside = false)
  }

  // steps aside when a format exists: kebs' value enum formats are neither more nor less specific than this one
  def autoSealed[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    val T = weakTypeOf[T]
    if (!isSealed(T)) c.abort(c.enclosingPosition, s"$T is not a sealed hierarchy")
    skipForImplicitSearch[T]()
    derive[T](consultExisting = true, stepAside = true)
  }

  def explicitProduct[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    if (!isCaseClass(weakTypeOf[T])) c.abort(c.enclosingPosition, s"${weakTypeOf[T]} is not a case class")
    derive[T](consultExisting = false, stepAside = false)
  }

  def explicitSealed[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    if (!isSealed(weakTypeOf[T])) c.abort(c.enclosingPosition, s"${weakTypeOf[T]} is not a sealed hierarchy")
    derive[T](consultExisting = false, stepAside = false)
  }

  // re-typed, so that the owners of the local formats are fixed before lambdalift sees the recursive references
  private def derive[T: c.WeakTypeTag](consultExisting: Boolean, stepAside: Boolean): c.Expr[RootJsonFormat[T]] =
    c.Expr[RootJsonFormat[T]](
      c.untypecheck(
        deriveRootFormat[T](c.Expr[JsonConfig](q"${c.prefix}.kebsJsonConfig"), consultExisting, stepAside).tree
      )
    )
}
