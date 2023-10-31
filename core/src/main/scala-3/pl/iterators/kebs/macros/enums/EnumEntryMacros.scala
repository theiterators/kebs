//package pl.iterators.kebs.macros.enums
//
//import pl.iterators.kebs.enums.ValueEnum
//import scala.quoted._
//import scala.compiletime.{constValue, erasedValue, error, summonInline}
//import scala.deriving.Mirror
//import scala.reflect.{ClassTag, Enum}
//import scala.collection.immutable
//
//trait EnumLike[T] {
//  def values: immutable.Seq[T]
//  def valueOf(name: String): T = values.find(_.toString == name).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
//  def fromOrdinal(ordinal: Int): T = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
//}
//
//class EnumOf[E](val `enum`: EnumLike[E])
//
//inline private def widen[A, B] (a: A): A & B =
//  inline a match {
//  case b: B => b
//  }
//
//object EnumOf {
//  inline given [E <: Enum](using m: Mirror.SumOf[E], ct: ClassTag[E]): EnumOf[E] = {
//    val enumValues = summonCases[m.MirroredElemTypes, E]
//    EnumOf[E](new EnumLike[E] {
//      override def values: immutable.Seq[E] = enumValues.toSeq
//    })
//  }
//
//  inline private def summonCases[T <: Tuple, A]: List[A] =
//    inline erasedValue[T] match {
//    case _: (h *: t) =>
//      (inline summonInline[Mirror.Of[h]] match {
//        case m: Mirror.Singleton =>
//          widen[m.MirroredMonoType, A](m.fromProduct(EmptyTuple)) :: summonCases[t, A]
//        case x => error("Enums cannot include parameterized cases.")
//      })
//
//    case _: EmptyTuple => Nil
//  }
//}
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