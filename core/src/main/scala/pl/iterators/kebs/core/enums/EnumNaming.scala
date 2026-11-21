package pl.iterators.kebs.core.enums

import pl.iterators.kebs.core.naming.Naming

// the implicit default lives in a supertype's companion, so `import EnumNaming._` cannot make it ambiguous
sealed abstract class EnumNamingDefaults
object EnumNamingDefaults {
  implicit val default: EnumNaming = EnumNaming(Naming.identity)
}

final case class EnumNaming(naming: Naming, caseInsensitive: Boolean = true) extends EnumNamingDefaults {
  def applyTo[E](enumLike: EnumLike[E]): EnumLike[E] =
    if (naming == Naming.identity) enumLike else EnumLike[E](enumLike.values, e => naming(enumLike.getName(e)))

  // an exact match first: names that differ only in case must not resolve to another entry
  def lookup[E](named: EnumLike[E], name: String): Option[E] =
    named.withNameOption(name) match {
      case None if caseInsensitive => named.withNameInsensitiveOption(name)
      case exact                   => exact
    }

  def lookupOrThrow[E](named: EnumLike[E], name: String): E =
    lookup(named, name).getOrElse(throw new NoSuchElementException(s"$name should be one of ${named.names.mkString(", ")}"))
}

object EnumNaming {
  val default: EnumNaming            = EnumNamingDefaults.default
  val uppercase: EnumNaming          = EnumNaming(Naming.uppercase)
  val lowercase: EnumNaming          = EnumNaming(Naming.lowercase)
  val snakeCase: EnumNaming          = EnumNaming(Naming.snakeCase)
  val screamingSnakeCase: EnumNaming = EnumNaming(Naming.screamingSnakeCase)
  val kebabCase: EnumNaming          = EnumNaming(Naming.kebabCase)
}
