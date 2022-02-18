package pl.iterators.kebs.macros.enums

import scala.quoted._

trait EnumLike[T] {
  def values: Array[T]
  def valueOf(name: String): T
  def fromOrdinal(ordinal: Int): T
}

class EnumOf[E](val `enum`: EnumLike[E])

object EnumOf {
  inline given [E]: EnumOf[E] = ${EnumOf.impl[E]()}

  private def impl[T]()(using Quotes, Type[T]): Expr[EnumOf[T]] = {
    import quotes.reflect._
    val companion = Ref(TypeRepr.of[T].typeSymbol.companionModule)
    '{
      new EnumOf(
        new EnumLike[T] {
          def values: Array[T] = ${Select.unique(companion, "values").asExprOf[Array[T]]}
          def valueOf(name: String): T = ${Apply(Select.unique(companion, "valueOf"), List('{name}.asTerm)).asExprOf[T]}
          def fromOrdinal(ordinal: Int): T = ${Apply(Select.unique(companion, "fromOrdinal"), List('{ordinal}.asTerm)).asExprOf[T]}
        }
      )
    }
  }
}