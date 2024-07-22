package pl.iterators.kebs.tagged.slick

import pl.iterators.kebs.tagged._
import _root_.slick.jdbc.JdbcProfile

trait KebsTaggedSlickSupport { this: JdbcProfile =>
  trait KebsTaggedImplicits {
    implicit def taggedColumnType[T, U](implicit baseColumnType: BaseColumnType[T]): BaseColumnType[T @@ U] = {
      MappedColumnType.base[T @@ U, T](identity, _.@@[U])
    }
  }
}
