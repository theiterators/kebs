package pl.iterators.kebs.core.enums

import scala.collection.immutable

trait EnumLike[T] {
  def valuesToNamesMap: Map[T, String]
  def values: immutable.Seq[T]                             = valuesToNamesMap.keys.toSeq
  def names: immutable.Seq[String]                         = valuesToNamesMap.values.toSeq
  def getNamesToValuesMap: Map[String, T]                  = valuesToNamesMap.map(_.swap)
  def withNameOption(name: String): Option[T]              = getNamesToValuesMap.get(name)
  def withNameUppercaseOnlyOption(name: String): Option[T] = valuesToNamesMap.find(_._2.toUpperCase == name).map(_._1)
  def withNameInsensitiveOption(name: String): Option[T]   = valuesToNamesMap.find(_._2.equalsIgnoreCase(name)).map(_._1)
  def withNameLowercaseOnlyOption(name: String): Option[T] = valuesToNamesMap.find(_._2.toLowerCase == name).map(_._1)
  def withNameUppercaseOnly(name: String): T               =
    withNameUppercaseOnlyOption(name).getOrElse(throw new NoSuchElementException(buildNotFoundMessage(name)))
  def withNameLowercaseOnly(name: String): T =
    withNameLowercaseOnlyOption(name).getOrElse(throw new NoSuchElementException(buildNotFoundMessage(name)))
  def withName(name: String): T =
    withNameOption(name).getOrElse(throw new NoSuchElementException(buildNotFoundMessage(name)))
  def getName(e: T): String =
    valuesToNamesMap.getOrElse(e, throw new NoSuchElementException(s"enum case not found for type: $e"))
  def valueOf(name: String): T =
    getNamesToValuesMap.getOrElse(name, throw new IllegalArgumentException(s"enum case not found: $name"))
  def valueOfIgnoreCase(name: String): T =
    values.find(_.toString.equalsIgnoreCase(name)).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
  def withNameIgnoreCase(name: String): T               = values.find(_.toString.equalsIgnoreCase((name))).get
  def withNameIgnoreCaseOption(name: String): Option[T] = values.find(_.toString.equalsIgnoreCase((name)))
  def fromOrdinal(ordinal: Int): T = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
  def indexOf(member: T): Int      = values.zipWithIndex.toMap.getOrElse(member, -1)

  private def buildNotFoundMessage(name: String): String = s"$name should be one of ${names.mkString(", ")}"
}
