package pl.iterators.kebs.core.enums

import pl.iterators.kebs.core.naming.Naming

final case class EnumNaming(naming: Naming, caseInsensitive: Boolean = true) {
  def applyTo[E](enumLike: EnumLike[E]): EnumLike[E] =
    if (naming == Naming.identity) enumLike else EnumLike[E](enumLike.values, e => naming(enumLike.getName(e)))

  def lookup[E](named: EnumLike[E], name: String): Option[E] =
    if (caseInsensitive) named.withNameInsensitiveOption(name) else named.withNameOption(name)

  def lookupOrThrow[E](named: EnumLike[E], name: String): E =
    lookup(named, name).getOrElse(throw new NoSuchElementException(s"$name should be one of ${named.names.mkString(", ")}"))
}

object EnumNaming {
  implicit val default: EnumNaming = EnumNaming(Naming.identity)

  val uppercase: EnumNaming          = EnumNaming(Naming.uppercase)
  val lowercase: EnumNaming          = EnumNaming(Naming.lowercase)
  val snakeCase: EnumNaming          = EnumNaming(Naming.snakeCase)
  val screamingSnakeCase: EnumNaming = EnumNaming(Naming.screamingSnakeCase)
  val kebabCase: EnumNaming          = EnumNaming(Naming.kebabCase)
}
