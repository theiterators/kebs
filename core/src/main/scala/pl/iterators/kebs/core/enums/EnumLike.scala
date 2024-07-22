package pl.iterators.kebs.core.enums

import scala.collection.immutable

trait EnumLike[T] {
  def values: immutable.Seq[T]
  def getNamesToValuesMap: Map[String, T]                  = EnumLike.namesToValuesMap(this)
  def withNameOption(name: String): Option[T]              = EnumLike.namesToValuesMap(this).get(name)
  def withNameUppercaseOnlyOption(name: String): Option[T] = EnumLike.upperCaseNameValuesToMap(this).get(name)
  def withNameInsensitiveOption(name: String): Option[T]   = EnumLike.lowerCaseNamesToValuesMap(this).get(name.toLowerCase)
  def withNameLowercaseOnlyOption(name: String): Option[T] = EnumLike.lowerCaseNamesToValuesMap(this).get(name)
  def withNameUppercaseOnly(name: String): T =
    withNameUppercaseOnlyOption(name).getOrElse(throw new NoSuchElementException(EnumLike.buildNotFoundMessage(name, this)))
  def withNameLowercaseOnly(name: String): T =
    withNameLowercaseOnlyOption(name).getOrElse(throw new NoSuchElementException(EnumLike.buildNotFoundMessage(name, this)))
  def withName(name: String): T =
    withNameOption(name).getOrElse(throw new NoSuchElementException(EnumLike.buildNotFoundMessage(name, this)))
  def valueOf(name: String): T =
    values.find(_.toString == name).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
  def valueOfIgnoreCase(name: String): T =
    values.find(_.toString.equalsIgnoreCase(name)).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
  def withNameIgnoreCase(name: String): T               = values.find(_.toString.equalsIgnoreCase((name))).get
  def withNameIgnoreCaseOption(name: String): Option[T] = values.find(_.toString.equalsIgnoreCase((name)))
  def fromOrdinal(ordinal: Int): T = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
  def indexOf(member: T): Int      = values.zipWithIndex.toMap.getOrElse(member, -1)
}

private[core] object EnumLike {
  private def namesToValuesMap[T](`enum`: EnumLike[T]): Map[String, T] = `enum`.values.map(v => v.toString -> v).toMap
  private def upperCaseNameValuesToMap[T](`enum`: EnumLike[T]): Map[String, T] = namesToValuesMap(`enum`).map { case (k, v) =>
    k.toUpperCase() -> v
  }
  private def lowerCaseNamesToValuesMap[T](`enum`: EnumLike[T]): Map[String, T] = namesToValuesMap(`enum`).map { case (k, v) =>
    k.toLowerCase() -> v
  }
  private def existingEntriesString[T](`enum`: EnumLike[T]): String = `enum`.values.map(_.toString).mkString(", ")
  private def buildNotFoundMessage[T](notFoundName: String, `enum`: EnumLike[T]): String =
    s"$notFoundName is not a member of Enum (${existingEntriesString(`enum`)})"
}
