package pl.iterators.kebs.core.naming

import pl.iterators.kebs.core.macros.namingconventions.{CapitalizeVariant, SnakifyVariant}

import java.util.concurrent.ConcurrentHashMap

final case class Naming(transform: String => String) {
  // derived codecs rename on every encode/decode; names are field and case names, so the set is small (the cap guards
  // against a naming applied to arbitrary runtime strings)
  private val memo = new ConcurrentHashMap[String, String]

  def apply(name: String): String = {
    val cached = memo.get(name)
    if (cached ne null) cached
    else {
      val renamed = transform(name)
      if (memo.size < Naming.MemoLimit) memo.put(name, renamed)
      renamed
    }
  }

  def andThen(that: Naming): Naming = Naming(transform.andThen(that.transform))
}

object Naming {
  private val MemoLimit = 4096

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
