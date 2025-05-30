package pl.iterators.kebs.core.enums

import scala.collection.immutable

trait ValueEnumLikeEntry[ValueType] {
  def value: ValueType
}

trait ValueEnumLike[ValueType, EntryType <: ValueEnumLikeEntry[ValueType]] {
  def values: immutable.Seq[EntryType]
  def getValuesToEntriesMap: Map[ValueType, EntryType] = ValueEnumLike.valuesToEntriesMap(this)
  def withValue(i: ValueType): EntryType               =
    withValueOption(i).getOrElse(throw new NoSuchElementException(ValueEnumLike.buildNotFoundMessage(this, i)))
  def withValueOption(i: ValueType): Option[EntryType] = ValueEnumLike.valuesToEntriesMap(this).get(i)
  def valueOf(value: ValueType): EntryType             =
    values.find(entry => value == entry.value).getOrElse(throw new IllegalArgumentException(s"enum case not found: $value"))
  def valueOfOption(value: ValueType): Option[EntryType] = values.find(entry => value == entry.value)
  def fromOrdinal(ordinal: Int): EntryType = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
  def indexOf(member: EntryType): Int      =
    values.zipWithIndex.find { case (entry, _) => member == entry }.map { case (_, index) => index }.getOrElse(-1)
}

private[core] object ValueEnumLike {
  private def valuesToEntriesMap[ValueType, EntryType <: ValueEnumLikeEntry[ValueType]](
      `enum`: ValueEnumLike[ValueType, EntryType]
  ): Map[ValueType, EntryType] = `enum`.values.map(v => v.value -> v).toMap
  private def existingEntriesString[ValueType, EntryType <: ValueEnumLikeEntry[ValueType]](
      `enum`: ValueEnumLike[ValueType, EntryType]
  ): String = `enum`.values.map(_.value).mkString(", ")
  private def buildNotFoundMessage[ValueType, EntryType <: ValueEnumLikeEntry[ValueType]](
      `enum`: ValueEnumLike[ValueType, EntryType],
      i: ValueType
  ): String = s"${i.toString} is not a member of ValueEnum (${existingEntriesString(`enum`)})"
}
