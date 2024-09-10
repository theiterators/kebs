package pl.iterators.kebs.tagged.slick

import pl.iterators.kebs.tagged._
import _root_.slick.jdbc.JdbcProfile
import scala.reflect.ClassTag

trait KebsTaggedSlickSupport { this: JdbcProfile =>
  trait KebsTaggedImplicits {
    implicit def taggedColumnType[T, U](implicit baseColumnType: BaseColumnType[T], cls: ClassTag[T @@ U]): ColumnType[T @@ U] = {
      MappedColumnType.base[T @@ U, T](identity, _.@@[U])
    }
  }
}
