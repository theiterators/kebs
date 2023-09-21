package pl.iterators.kebs.enumeratum

import pl.iterators.kebs.enums.{ValueEnumLike}
import scala.collection.immutable
import enumeratum.values._
import scala.quoted._
import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving._
import scala.reflect.{ClassTag, Enum}

class ValueEnumOf[V, E <: ValueEnumEntry[V]](val `enum`: ValueEnumLike[V, E])

inline private def widen[A, B] (a: A): A & B =
  inline a match {
    case b: B => b
  }

object ValueEnumOf {
  inline given [V, E <: ValueEnumEntry[V]](using m: Mirror.SumOf[E], ct: ClassTag[E]): ValueEnumOf[V, E] = {
    val enumValues = summonValueCases[m.MirroredElemTypes, V, E]
    ValueEnumOf[V, E](new ValueEnumLike[V, E] {
      override def values: immutable.Seq[E] = enumValues
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