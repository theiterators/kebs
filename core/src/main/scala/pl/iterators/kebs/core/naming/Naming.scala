package pl.iterators.kebs.core.naming

import pl.iterators.kebs.core.macros.namingconventions.{CapitalizeVariant, SnakifyVariant}

final case class Naming(transform: String => String) {
  def apply(name: String): String   = transform(name)
  def andThen(that: Naming): Naming = Naming(transform.andThen(that.transform))
}

object Naming {
  val identity: Naming           = Naming(name => name)
  val snakeCase: Naming          = Naming(SnakifyVariant.snakify)
  val screamingSnakeCase: Naming = Naming(SnakifyVariant.snakify(_).toUpperCase)
  val kebabCase: Naming          = Naming(SnakifyVariant.snakify(_).replace('_', '-'))
  val camelCase: Naming          = Naming { name =>
    SnakifyVariant.snakify(name).split('_').filter(_.nonEmpty).toList match {
      case first :: rest => (first :: rest.map(_.capitalize)).mkString
      case Nil           => name
    }
  }
  val pascalCase: Naming = Naming(CapitalizeVariant.capitalize)
  val lowercase: Naming  = Naming(_.toLowerCase)
  val uppercase: Naming  = Naming(_.toUpperCase)

  def custom(f: String => String): Naming = Naming(f)
}
