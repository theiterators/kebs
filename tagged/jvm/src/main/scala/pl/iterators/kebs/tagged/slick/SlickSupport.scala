package pl.iterators.kebs.tagged.slick

import pl.iterators.kebs.tagged._
import _root_.slick.jdbc.JdbcProfile

trait SlickSupport {
  implicit def taggedColumnType[T, U](implicit bct: JdbcProfile#BaseColumnType[T], jp: JdbcProfile): _root_.slick.jdbc.JdbcTypesComponent#MappedJdbcType[T @@ U, T] = jp.MappedColumnType.base[T @@ U, T](identity, _.@@[U]).asInstanceOf[_root_.slick.jdbc.JdbcTypesComponent#MappedJdbcType[T @@ U, T]]
}
