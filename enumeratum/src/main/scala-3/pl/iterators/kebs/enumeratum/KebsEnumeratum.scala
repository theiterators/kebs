package pl.iterators.kebs.enumeratum

import enumeratum._
import scala.collection.immutable
import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror
import scala.reflect.ClassTag

import pl.iterators.kebs.core.enums.EnumLike

trait KebsEnumeratum {
  inline given [E <: EnumEntry](using m: Mirror.SumOf[E], ct: ClassTag[E]): EnumLike[E] = {
    val enumValues = summonCases[m.MirroredElemTypes, E]
    new EnumLike[E] {
      override def values: immutable.Seq[E] = enumValues.toSeq
    }
  }
}

inline private def widen[A, B](a: A): A & B =
  inline a match {
    case b: B => b
  }

inline private def summonCases[T <: Tuple, A]: List[A] =
  inline erasedValue[T] match {
    case _: (h *: t) =>
      (inline summonInline[Mirror.Of[h]] match {
        case m: Mirror.Singleton =>
          widen[m.MirroredMonoType, A](m.fromProduct(EmptyTuple)) :: summonCases[t, A]
        case x => error("Enums cannot include parameterized cases.")
      })

    case _: EmptyTuple => Nil
  }
