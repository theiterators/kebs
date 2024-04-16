package pl.iterators.kebs.enumeratum

import scala.collection.immutable
import enumeratum.values._
import scala.quoted._
import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving._
import scala.reflect.{ClassTag, Enum}

import pl.iterators.kebs.core.enums.{ValueEnumLike, ValueEnumLikeEntry}

trait KebsValueEnumeratum {
  inline implicit def valueEnumLike[V, E <: ValueEnumEntry[V] with ValueEnumLikeEntry[V]](using m: Mirror.SumOf[E], ct: ClassTag[E]): ValueEnumLike[V, E] = {
    val enumValues = summonValueCases[m.MirroredElemTypes, V, E]
    new ValueEnumLike[V, E] {
      override def values: immutable.Seq[E] = enumValues
    }
  }
}

inline private def widen[A, B] (a: A): A & B =
  inline a match {
    case b: B => b
  }

inline private def summonValueCases[T <: Tuple, V, A <: ValueEnumEntry[V]]: List[A] =
  inline erasedValue[T] match {
  case _: (h *: t) =>
    (inline summonInline[Mirror.Of[h]] match {
      case m: Mirror.Singleton =>
        widen[m.MirroredMonoType, A](m.fromProduct(EmptyTuple)) :: summonValueCases[t, V, A]
      case x => error("Enums cannot include parameterized cases.")
    })

  case _: EmptyTuple => Nil
}
