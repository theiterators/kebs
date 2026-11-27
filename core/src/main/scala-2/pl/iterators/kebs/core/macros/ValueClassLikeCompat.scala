package pl.iterators.kebs.core.macros

import pl.iterators.kebs.core.macros.internal.ValueClassLikeMacrosImpl

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[macros] trait ValueClassLikeCompat {
  implicit def repFromCaseClass[T <: Product, F](implicit m: ValueClass[T]): ValueClassLike[T, F] = macro ValueClassRepMacros.rep[T, F]
}

private[macros] trait ValueClassCompat {
  def derived[VC]: ValueClass[VC] = macro ValueClassRepMacros.marker[VC]
}

// whitebox, so that the expansion's type fixes F1 left open by the caller
final private[macros] class ValueClassRepMacros(val c: whitebox.Context) extends hearth.MacroCommonsScala2 with ValueClassLikeMacrosImpl {
  // with CaseClass1ToValueClass, tried in every search for a ValueClassLike of a Product, so it gives up before hearth's set-up
  def rep[VC <: Product: c.WeakTypeTag, F1: c.WeakTypeTag](
      m: c.Expr[ValueClass[VC]]
  ): c.Expr[ValueClassLike[VC, F1]] = {
    val VC     = c.weakTypeOf[VC]
    val symbol = VC.dealias.typeSymbol
    if (!symbol.isClass || !symbol.asClass.isCaseClass || symbol.isModuleClass)
      c.abort(c.enclosingPosition, s"${symbol.name} is not a case class")
    if (symbol.asClass.primaryConstructor.asMethod.paramLists.flatten.size != 1)
      c.abort(c.enclosingPosition, s"To derive ValueClassLike, ${symbol.name} must have exactly one field")
    c.Expr[ValueClassLike[VC, F1]](plain(VC).getOrElse(deriveValueClassLike[VC].value.tree))
  }

  // The usual case without hearth, whose set-up costs more than the rest of an expansion, made at each search: a public case
  // class with a public field and no apply of its own, built with its constructor as its synthetic apply would be.
  private def plain(VC: c.Type): Option[c.Tree] = {
    import c.universe._
    val cls       = VC.dealias.typeSymbol.asClass
    val companion = cls.companion
    // by the parameter's name: a private field has a public accessor of another name
    val parameter = cls.primaryConstructor.asMethod.paramLists.flatten.head
    def ownApply  = companion.typeSignature.member(TermName("apply")).alternatives.exists(!_.isSynthetic)
    VC.decl(parameter.name) match {
      case field: MethodSymbol
          if field.isCaseAccessor && field.isPublic && cls.primaryConstructor.isPublic && companion != NoSymbol && !ownApply =>
        val F = field.typeSignatureIn(VC).finalResultType
        Some(
          q"new _root_.pl.iterators.kebs.core.macros.ValueClassLike[$VC, $F]((value: $F) => new $VC(value), (obj: $VC) => obj.${field.name})"
        )
      case _ => None
    }
  }

  def marker[VC: c.WeakTypeTag]: c.Expr[ValueClass[VC]] = {
    checkValueClass[VC]
    c.Expr[ValueClass[VC]](c.universe.reify(ValueClass.unchecked[VC]).tree)
  }
}
