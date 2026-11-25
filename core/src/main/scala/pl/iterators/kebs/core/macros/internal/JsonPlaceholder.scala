package pl.iterators.kebs.core.macros.internal

import scala.annotation.compileTimeOnly

// what an entry point returns to a root that is only finding out what it needs; never part of the generated code
object JsonPlaceholder {
  @compileTimeOnly("a kebs derivation left a placeholder behind; please report it")
  def apply[A](key: String): A = throw new IllegalStateException(s"No instance: $key")

  @compileTimeOnly("kebs cannot derive this inside another implicit search with this compiler; derive it on its own")
  def unavailable[A]: A = throw new IllegalStateException("Not derived")
}
