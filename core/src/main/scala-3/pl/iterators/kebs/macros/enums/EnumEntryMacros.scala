//package pl.iterators.kebs.macros.enums
//
//import pl.iterators.kebs.enums.ValueEnum
//import scala.quoted._
//import scala.compiletime.{constValue, erasedValue, error, summonInline}
//import scala.deriving.Mirror
//import scala.reflect.{ClassTag, Enum}
//import scala.collection.immutable
//
//trait ValueEnumLike[ValueType, T <: ValueEnum[ValueType]] {
//  def values: immutable.Seq[T]
//  def valueOf(value: ValueType): T = values.find(_.value == value).getOrElse(throw new IllegalArgumentException(s"enum case not found: $value"))
//  def fromOrdinal(ordinal: Int): T = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
//}
//
//class ValueEnumOf[V, E <: ValueEnum[V]](val `enum`: ValueEnumLike[V, E])
//
//object ValueEnumOf {
//  inline given [V, E <: ValueEnum[V] with Enum](using m: Mirror.SumOf[E], ct: ClassTag[E]): ValueEnumOf[V, E] = {
//    val enumValues = summonValueCases[m.MirroredElemTypes, V, E]
//    ValueEnumOf[V, E](new ValueEnumLike[V, E] {
//      override def values: immutable.Seq[E] = enumValues.toSeq
//    })
//  }
//
//  inline private def summonValueCases[T <: Tuple, V, A <: ValueEnum[V]]: List[A] =
//    inline erasedValue[T] match {
//    case _: (h *: t) =>
//      (inline summonInline[Mirror.Of[h]] match {
//        case m: Mirror.Singleton =>
//          widen[m.MirroredMonoType, A](m.fromProduct(EmptyTuple)) :: summonValueCases[t, V, A]
//        case x => error("Enums cannot include parameterized cases.")
//      })
//
//    case _: EmptyTuple => Nil
//  }
//}