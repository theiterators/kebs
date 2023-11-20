//package pl.iterators.kebs.enums
//
//import scala.collection.immutable
//
//trait ValueEnumLikeEntry[ValueType] {
//  def value: ValueType
//}
//
//
//trait ValueEnumLike[ValueType, EntryType <: ValueEnumLikeEntry[ValueType]] {
//  def values: immutable.Seq[EntryType]
//  final lazy val valuesToEntriesMap: Map[ValueType, EntryType] = values.map(v => v.value -> v).toMap
//  private lazy val existingEntriesString = values.map(_.value).mkString(", ")
//  private def buildNotFoundMessage(i: ValueType): String = s"${i.toString} is not a member of ValueEnum ($existingEntriesString)"
//  def withValue(i: ValueType): EntryType = withValueOpt(i).getOrElse(throw new NoSuchElementException(buildNotFoundMessage(i)))
//  def withValueOpt(i: ValueType): Option[EntryType] = valuesToEntriesMap.get(i)
//  def valueOf(value: ValueType): EntryType = values.find(entry => value == entry.value).getOrElse(throw new IllegalArgumentException(s"enum case not found: $value"))
//  def valueOfOpt(value: ValueType): Option[EntryType] = values.find(entry => value == entry.value)
//  def valueOfUnsafe(value: ValueType): EntryType = values.find(entry => value == entry.value).get
//  def fromOrdinal(ordinal: Int): EntryType = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
//  def indexOf(member: EntryType): Int = values.zipWithIndex.find { case (entry, _) => member == entry }.map { case (_, index) => index }.getOrElse(-1)
//}
