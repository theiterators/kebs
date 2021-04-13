package pl.iterators.kebs.hstore

import slick.ast.Library.{SqlFunction, SqlOperator}
import slick.ast.ScalaBaseType.booleanType
import slick.ast.TypedType
import slick.jdbc.JdbcType
import slick.lifted.{ExtensionMethods, Rep}

class KebsHStoreColumnExtensionMethods[KEY, VALUE, P1](val c: Rep[P1])(
    implicit tm0: JdbcType[KEY],
    tm1: JdbcType[VALUE],
    tm2: JdbcType[List[KEY]],
    tm3: JdbcType[List[VALUE]],
    tm4: JdbcType[Map[KEY, VALUE]]
) extends ExtensionMethods[Map[KEY, VALUE], P1] {

  protected implicit def b1Type: TypedType[Map[KEY, VALUE]] = implicitly[TypedType[Map[KEY, VALUE]]]

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

  def ??[P2, R](k: Rep[P2])(implicit om: o#arg[KEY, P2]#to[Boolean, R]) = {
    om.column(Exist, n, k.toNode)
  }
}
