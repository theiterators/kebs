package pl.iterators.kebs.core.enums

import scala.collection.immutable

trait EnumLike[T] {
  def values: immutable.Seq[T]
  lazy val namesToValuesMap: Map[String, T] = values.map(v => v.toString -> v).toMap ++ extraNamesToValuesMap
  def extraNamesToValuesMap: Map[String, T] = Map.empty[String, T]
  def withNameOption(name: String): Option[T] = namesToValuesMap.get(name)
  lazy final val upperCaseNameValuesToMap: Map[String, T] = namesToValuesMap.map { case (k, v) => k.toUpperCase() -> v }
  lazy final val lowerCaseNamesToValuesMap: Map[String, T] = namesToValuesMap.map { case (k, v) => k.toLowerCase() -> v }
  def withNameUppercaseOnlyOption(name: String): Option[T] = upperCaseNameValuesToMap.get(name)
  def withNameInsensitiveOption(name: String): Option[T] = lowerCaseNamesToValuesMap.get(name.toLowerCase)
  def withNameLowercaseOnlyOption(name: String): Option[T] = lowerCaseNamesToValuesMap.get(name)
  def withNameUppercaseOnly(name: String): T = withNameUppercaseOnlyOption(name).getOrElse(throw new NoSuchElementException(buildNotFoundMessage(name)))
  def withNameLowercaseOnly(name: String): T = withNameLowercaseOnlyOption(name).getOrElse(throw new NoSuchElementException(buildNotFoundMessage(name)))
  private lazy val existingEntriesString = values.map(_.toString).mkString(", ")
  private def buildNotFoundMessage(notFoundName: String): String = s"$notFoundName is not a member of Enum ($existingEntriesString)"
  def withName(name: String): T = withNameOption(name).getOrElse(throw new NoSuchElementException(buildNotFoundMessage(name)))
  def valueOf(name: String): T = values.find(_.toString == name).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
  def withNameUnsafe(name: String): T       = values.find(_.toString == name).get
  def withNameSafe(name: String): Option[T] = values.find(_.toString == name)
  def valueOfIgnoreCase(name: String): T = values.find(_.toString.equalsIgnoreCase(name)).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
  def withNameIgnoreCaseUnsafe(name: String): T       = values.find(_.toString.equalsIgnoreCase((name))).get
  def withNameIgnoreCaseSafe(name: String): Option[T] = values.find(_.toString.equalsIgnoreCase((name)))
  def fromOrdinal(ordinal: Int): T                    = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
  def indexOf(member: T): Int                         = values.zipWithIndex.toMap.getOrElse(member, -1)
}
