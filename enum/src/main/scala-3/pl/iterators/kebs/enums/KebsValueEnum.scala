package pl.iterators.kebs.enums

import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror

import pl.iterators.kebs.core.enums.{ValueEnumLike, ValueEnumLikeEntry}

trait KebsValueEnum {
  inline implicit def kebsValueEnumScala[V, E <: ValueEnumLikeEntry[V]](using
      m: Mirror.SumOf[E]
  ): ValueEnumLike[V, E] = {
    val enumValues = summonValueCases[m.MirroredElemTypes, V, E]
    ValueEnumLike[V, E](enumValues)
  }
}

inline private def summonValueCases[T <: Tuple, V, A <: ValueEnumLikeEntry[V]]: List[A] =
  inline erasedValue[T] match {
    case _: (h *: t) =>
      (inline summonInline[Mirror.Of[h]] match {
        case m: Mirror.Singleton =>
          widen[m.MirroredMonoType, A](m.fromProduct(EmptyTuple)) :: summonValueCases[t, V, A]
        case x => error("Enums cannot include parameterized cases.")
      })

    case _: EmptyTuple => Nil
  }
