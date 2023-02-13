package pl.iterators.kebs.macros.enums

import scala.quoted._
import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror
import scala.reflect.{ClassTag, Enum}

trait EnumLike[T] {
  def values: Array[T]
  def valueOf(name: String): T
  def fromOrdinal(ordinal: Int): T
}

class EnumOf[E](val `enum`: EnumLike[E])

object EnumOf {
  inline given [E <: Enum](using m: Mirror.SumOf[E], ct: ClassTag[E]): EnumOf[E] = {
    val enumValues = summonCases[m.MirroredElemTypes, E]
    EnumOf[E](new EnumLike[E] {
      override def values: Array[E] = enumValues.toArray
      override def valueOf(name: String): E = enumValues.find(_.toString == name).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
      override def fromOrdinal(ordinal: Int): E = enumValues.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
    })
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
}