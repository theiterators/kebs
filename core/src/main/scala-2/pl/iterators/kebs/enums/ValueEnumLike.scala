package pl.iterators.kebs.enums

import scala.collection.immutable
import scala.language.reflectiveCalls

trait ValueEnumLike[ValueType, EntryType <: { def value: ValueType }] {
  def values: immutable.Seq[EntryType]
  def valueOf(value: ValueType): EntryType = values.find(entry => value == entry.value).getOrElse(throw new IllegalArgumentException(s"enum case not found: $value"))
  def valueOfOpt(value: ValueType): Option[EntryType] = values.find(entry => value == entry.value)
  def valueOfUnsafe(value: ValueType): EntryType = values.find(entry => value == entry.value).get
  def fromOrdinal(ordinal: Int): EntryType = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
  def indexOf(member: EntryType): Int = values.zipWithIndex.find { case (entry, _) => member == entry }.map { case (_, index) => index }.getOrElse(-1)
}
