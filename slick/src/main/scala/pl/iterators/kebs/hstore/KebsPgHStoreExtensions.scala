package pl.iterators.kebs.hstore

import slick.ast.Library.{SqlFunction, SqlOperator}
import slick.ast.TypedType
import slick.jdbc.{JdbcType, JdbcTypesComponent, PostgresProfile}
import slick.lifted.{ExtensionMethods, FunctionSymbolExtensionMethods}

trait KebsPgHStoreExtensions extends JdbcTypesComponent { driver: PostgresProfile =>
  import FunctionSymbolExtensionMethods._
  import driver.api._

  object HStoreLibrary {
    val On          = new SqlOperator("->")
    val Exist       = new SqlOperator("??")
    val ExistAll    = new SqlOperator("??&")
    val ExistAny    = new SqlOperator("??|")
    val Defined     = new SqlFunction("defined")
    val Contains    = new SqlOperator("@>")
    val ContainedBy = new SqlOperator("<@")

    val Concatenate = new SqlOperator("||")
    val Delete      = new SqlOperator("-")
    val Slice       = new SqlFunction("slice")
  }

  /** Extension methods for hstore Columns */
  class HStoreColumnExtensionMethods[B0, B1, SEQ[B1], MAP[B0, B1], P1](val c: Rep[P1])(implicit t0: JdbcType[B0],
                                                                                       t1: JdbcType[B1],
                                                                                       tm: JdbcType[MAP[B0, B1]],
                                                                                       ts: JdbcType[SEQ[B1]])
      extends ExtensionMethods[MAP[B0, B1], P1] {

    protected implicit def b1Type: TypedType[MAP[B0, B1]] = implicitly[TypedType[MAP[B0, B1]]]

    def +>[P2, R](k: Rep[P2])(implicit om: o#arg[B0, P2]#to[B1, R]) = {
      HStoreLibrary.On.column[Option[B1]](n, k.toNode)
    }

  }
}
