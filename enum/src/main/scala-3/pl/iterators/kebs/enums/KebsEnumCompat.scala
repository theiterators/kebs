package pl.iterators.kebs.enums

import pl.iterators.kebs.core.enums.{EnumLike, ValueEnumLike, ValueEnumLikeEntry}
import pl.iterators.kebs.core.macros.internal.MirrorCases
import pl.iterators.kebs.enums.internal.EnumMacrosImpl

import scala.deriving.Mirror
import scala.quoted.*

trait KebsEnum {
  inline implicit def kebsEnumScala[E <: scala.reflect.Enum](using m: Mirror.SumOf[E]): EnumLike[E] = ${ EnumMacros.enumLike[E] }
  // transparent: an abstract E (no Enumeration object to list) must skip the candidate instead of failing
  transparent inline implicit def kebsEnumerationScala[E <: Enumeration#Value]: EnumLike[E] = ${ EnumMacros.enumerationLike[E] }
}

trait KebsValueEnum {
  inline implicit def kebsValueEnumScala[V, E <: ValueEnumLikeEntry[V]](using m: Mirror.SumOf[E]): ValueEnumLike[V, E] = ${
    EnumMacros.valueEnumLike[V, E]
  }
}

final private[enums] class EnumMacros(q: Quotes) extends hearth.MacroCommonsScala3(using q), EnumMacrosImpl

private[enums] object EnumMacros {
  def enumLike[E: Type](using q: Quotes): Expr[EnumLike[E]] =
    new EnumMacros(q).deriveEnumLike[E](mirrorCases[E])

  def enumerationLike[E: Type](using q: Quotes): Expr[EnumLike[E]] = {
    import q.reflect.*
    // Obj.values, as in kebs 2: id order, inherited values, alias vals listed once
    val values = TypeRepr.of[E].dealias match {
      case TypeRef(prefix: TermRef, _) if prefix.termSymbol.flags.is(Flags.Module) =>
        '{ ${ Ref.term(prefix).asExprOf[Enumeration] }.values.toList.asInstanceOf[List[E]] }
      case TypeRef(ThisType(module), _) if module.typeSymbol.flags.is(Flags.Module) =>
        '{ ${ Ref(module.typeSymbol.companionModule).asExprOf[Enumeration] }.values.toList.asInstanceOf[List[E]] }
      case _ => report.errorAndAbort(s"${Type.show[E]} is not a value of a scala.Enumeration object")
    }
    new EnumMacros(q).deriveEnumLike[E](values)
  }

  def valueEnumLike[V: Type, E <: ValueEnumLikeEntry[V]: Type](using q: Quotes): Expr[ValueEnumLike[V, E]] = {
    val cases = mirrorCases[E]
    '{ ValueEnumLike[V, E]($cases) }
  }

  private def mirrorCases[E: Type](using q: Quotes): Expr[List[E]] =
    MirrorCases
      .of[E]
      .getOrElse(q.reflect.report.errorAndAbort(s"${Type.show[E]} must be an enum whose cases are all objects (no parameters)"))
}
