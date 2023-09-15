package pl.iterators.kebs.enums

import pl.iterators.kebs.enums.{ValueEnumLike, ValueEnumLikeEntry}
import scala.collection.immutable
import scala.quoted._
import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror
import scala.reflect.{ClassTag, Enum}

class ValueEnumOf[V, E <: ValueEnumLikeEntry[V]](val `enum`: ValueEnumLike[V, E])

object ValueEnumOf {
  inline given [V, E <: ValueEnumLikeEntry[V] with Enum](using m: Mirror.SumOf[E], ct: ClassTag[E]): ValueEnumOf[V, E] = {
    val enumValues = summonValueCases[m.MirroredElemTypes, V, E]
    ValueEnumOf[V, E](new ValueEnumLike[V, E] {
      override def values: immutable.Seq[E] = enumValues.toSeq
    })
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
}