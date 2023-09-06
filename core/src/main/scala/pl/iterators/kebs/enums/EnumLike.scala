package pl.iterators.kebs.enums

import scala.collection.immutable

trait EnumLike[T] {
  def values: immutable.Seq[T]
  def valueOf(name: String): T = values.find(_.toString == name).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
  def withNameUnsafe(name: String): T = values.find(_.toString == name).get
  def withNameSafe(name: String): Option[T] = values.find(_.toString == name)
  def valueOfIgnoreCase(name: String): T = values.find(_.toString.equalsIgnoreCase(name)).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
  def withNameIgnoreCaseUnsafe(name: String): T = values.find(_.toString.equalsIgnoreCase((name))).get
  def withNameIgnoreCaseSafe(name: String): Option[T] = values.find(_.toString.equalsIgnoreCase((name)))
  def fromOrdinal(ordinal: Int): T = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
  def indexOf(member: T): Int = values.zipWithIndex.toMap.getOrElse(member, -1)
}