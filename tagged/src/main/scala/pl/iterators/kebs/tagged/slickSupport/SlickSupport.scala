package pl.iterators.kebs.tagged.slickSupport

import pl.iterators.kebs.tagged._
import slick.lifted.Isomorphism

trait SlickSupport {
  implicit def taggedColumnType[T, U]: Isomorphism[T @@ U, T] = new Isomorphism[T @@ U, T](identity, _.@@[U])
}

object SlickSupport extends SlickSupport
