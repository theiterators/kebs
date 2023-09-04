package pl.iterators.kebs.enums

trait ValueEnumLikeEntry[ValueType] {
  def value: ValueType
}

trait ValueEnumLike[ValueType, EntryType <: ValueEnumLikeEntry[ValueType]] {
  def values: Array[EntryType]
  def valueOf(value: ValueType): EntryType = values.find(enum => value == enum.value).getOrElse(throw new IllegalArgumentException(s"enum case not found: $value"))
  def valueOfOpt(value: ValueType): Option[EntryType] = values.find(enum => value == enum.value)
  def valueOfUnsafe(value: ValueType): EntryType = values.find(enum => value == enum.value).get
  def fromOrdinal(ordinal: Int): EntryType = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
  def indexOf(member: EntryType): Int = values.zipWithIndex.find { case (enum, _) => member == enum }.map { case (_, index) => index }.getOrElse(-1)
}
