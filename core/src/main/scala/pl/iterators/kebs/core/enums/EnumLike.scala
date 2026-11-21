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
    withNameInsensitiveOption(name).getOrElse(throw new IllegalArgumentException(s"enum case not found: $name"))
  def withNameIgnoreCase(name: String): T =
    withNameInsensitiveOption(name).getOrElse(throw new NoSuchElementException(buildNotFoundMessage(name)))
  def withNameIgnoreCaseOption(name: String): Option[T] = withNameInsensitiveOption(name)
  def fromOrdinal(ordinal: Int): T = values.lift(ordinal).getOrElse(throw new NoSuchElementException(ordinal.toString))
  def indexOf(member: T): Int      = values.zipWithIndex.toMap.getOrElse(member, -1)

  private def buildNotFoundMessage(name: String): String = s"$name should be one of ${names.mkString(", ")}"
}

object EnumLike {
  def apply[T](entries: => immutable.Seq[T], name: T => String): EnumLike[T] = new EnumLike[T] {
    override lazy val values: immutable.Seq[T]                        = entries
    override lazy val valuesToNamesMap: Map[T, String]                = values.map(v => v -> name(v)).toMap
    override lazy val getNamesToValuesMap: Map[String, T]             = values.map(v => name(v) -> v).toMap
    override lazy val names: immutable.Seq[String]                    = values.map(name)
    private lazy val indices: Map[T, Int]                             = values.zipWithIndex.toMap
    private lazy val byLowercaseName: Map[String, T]                  = firstByKey(_.toLowerCase)
    private lazy val byUppercaseName: Map[String, T]                  = firstByKey(_.toUpperCase)
    override def getName(e: T): String                                = name(e)
    override def withNameOption(name: String): Option[T]              = getNamesToValuesMap.get(name)
    override def indexOf(member: T): Int                              = indices.getOrElse(member, -1)
    override def withNameInsensitiveOption(name: String): Option[T]   = byLowercaseName.get(name.toLowerCase)
    override def withNameUppercaseOnlyOption(name: String): Option[T] = byUppercaseName.get(name)
    override def withNameLowercaseOnlyOption(name: String): Option[T] = byLowercaseName.get(name)

    // declaration order, the first entry wins when two names map to the same key
    private def firstByKey(key: String => String): Map[String, T] =
      values.foldLeft(Map.empty[String, T]) { (acc, v) =>
        val k = key(name(v))
        if (acc.contains(k)) acc else acc.updated(k, v)
      }
  }
}
