package pl.iterators.kebs.sprayjson

import pl.iterators.kebs.core.json.JsonConfig
import pl.iterators.kebs.sprayjson.internal.SprayFormatMacrosImpl
import spray.json.RootJsonFormat

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[sprayjson] trait KebsSprayJsonAutoCompat { self: KebsSprayJson =>
  implicit def jsonFormatN[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.jsonFormatN[T]

  @deprecated("jsonFormatN handles recursive types", "3.0.0")
  final def jsonFormatRec[T]: RootJsonFormat[T] = macro KebsSprayJsonMacros.jsonFormatN[T]
}

final private[sprayjson] class KebsSprayJsonMacros(val c: whitebox.Context) extends hearth.MacroCommonsScala2 with SprayFormatMacrosImpl {
  import c.universe.{Type => UType, _}

  override protected def isEnclosingDefinition(expr: UntypedExpr): Boolean = {
    val referenced                       = expr.symbol
    def accessed(symbol: Symbol): Symbol =
      if (symbol != null && symbol.isMethod && symbol.asMethod.isGetter) symbol.asMethod.accessed else symbol
    referenced != null && referenced != NoSymbol &&
    Iterator
      .iterate(c.internal.enclosingOwner)(_.owner)
      .takeWhile(owner => owner != NoSymbol && !owner.isClass && !owner.isPackage)
      .exists(owner => owner == referenced || accessed(owner) == accessed(referenced))
  }

  private def isLookingFor(tpe: UType): Boolean = c.enclosingImplicits.headOption.exists(_.pt.typeSymbol == tpe.typeSymbol)

  // jsonFormatN is more specific than jsonFlatFormat, so it steps aside when a JsonFormat/Reader/Writer of a value class
  // is wanted (kebs 2's rule); a RootJsonFormat request derives an object
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

  def jsonFormatN[T: c.WeakTypeTag]: c.Expr[RootJsonFormat[T]] = {
    val T      = weakTypeOf[T]
    val symbol = T.typeSymbol
    if (!(symbol.isClass && symbol.asClass.isCaseClass)) c.abort(c.enclosingPosition, s"$T is not a case class")
    if (flatPreferred(T)) c.abort(c.enclosingPosition, "Flat format preferred")
    // re-typed, so that the owners of the local formats are fixed before lambdalift sees the recursive references
    c.Expr[RootJsonFormat[T]](c.untypecheck(deriveRootFormat[T](c.Expr[JsonConfig](q"${c.prefix}.kebsJsonConfig")).tree))
  }
}
