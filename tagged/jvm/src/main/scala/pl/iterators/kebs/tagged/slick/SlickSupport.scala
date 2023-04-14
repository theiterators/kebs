package pl.iterators.kebs.tagged.slick

import slick.lifted.Isomorphism
import pl.iterators.kebs.tagged._

trait SlickSupport {
  implicit def taggedColumnType[T, U]: Isomorphism[T @@ U, T] = new Isomorphism[T @@ U, T](identity, _.@@[U])
}
