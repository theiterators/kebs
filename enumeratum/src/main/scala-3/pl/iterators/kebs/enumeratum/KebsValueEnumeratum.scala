package pl.iterators.kebs.enumeratum

import pl.iterators.kebs.enums.{ValueEnumLike, ValueEnumLikeEntry}
import scala.collection.immutable
import enumeratum.values._
import scala.quoted._
import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving._
import scala.reflect.{ClassTag, Enum}

class ValueEnumOf[V, E <: ValueEnumLikeEntry[V]](val `enum`: ValueEnumLike[V, E])

inline private def widen[A, B] (a: A): A & B =
  inline a match {
    case b: B => b
  }

object ValueEnumOf {
  inline given [V, E <: ValueEnumEntry[V]](using ct: ClassTag[E]): ValueEnumOf[V, ValueEnumLikeEntry[V]] = {
    val enumValues = summonValueCases[m.MirroredElemTypes, V, E]
    ValueEnumOf[V, ValueEnumLikeEntry[V]](new ValueEnumLike[V, ValueEnumLikeEntry[V]] {
      override def values: immutable.Seq[ValueEnumLikeEntry[V]] = enumValues.map(item =>
        new ValueEnumLikeEntry[V] {
          override def value: V = item.value
        })
    })
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
}