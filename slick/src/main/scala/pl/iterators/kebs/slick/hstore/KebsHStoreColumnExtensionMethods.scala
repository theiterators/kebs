package pl.iterators.kebs.slick.hstore

import slick.ast.Library.{SqlFunction, SqlOperator}
import slick.ast.ScalaBaseType._
import slick.ast.{Library, TypedType}
import slick.jdbc.JdbcType
import slick.lifted.{ExtensionMethods, FunctionSymbolExtensionMethods, Rep}

class KebsHStoreColumnExtensionMethods[KEY, VALUE, P1](val c: Rep[P1])(
    implicit tm1: JdbcType[VALUE],
    tm4: JdbcType[Map[KEY, VALUE]]
) extends ExtensionMethods[Map[KEY, VALUE], P1] {
  import FunctionSymbolExtensionMethods._

  protected implicit def b1Type: TypedType[Map[KEY, VALUE]] = implicitly[TypedType[Map[KEY, VALUE]]]

  object KebsHStoreLibrary {
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

  def +>[P2, R](k: Rep[P2]) = {
    KebsHStoreLibrary.On.column[Option[VALUE]](n, k.toNode)
  }
  def >>[T: JdbcType](k: Rep[KEY]) = {
    Library.Cast.column[T](KebsHStoreLibrary.On.column[VALUE](n, k.toNode).toNode)
  }
  def ??[P2, R](k: Rep[P2])(implicit om: o#arg[KEY, P2]#to[Boolean, R]) = {
    om.column(KebsHStoreLibrary.Exist, n, k.toNode)
  }
  def ?*[P2, R](k: Rep[P2])(implicit om: o#arg[KEY, P2]#to[Boolean, R]) = {
    om.column(KebsHStoreLibrary.Defined, n, k.toNode)
  }
  def ?|[P2, R](k: Rep[P2])(implicit om: o#arg[List[KEY], P2]#to[Boolean, R]) = {
    om.column(KebsHStoreLibrary.ExistAny, n, k.toNode)
  }
  def ?&[P2, R](k: Rep[P2])(implicit om: o#arg[List[KEY], P2]#to[Boolean, R]) = {
    om.column(KebsHStoreLibrary.ExistAll, n, k.toNode)
  }
  def @>[P2, R](c2: Rep[P2])(implicit om: o#arg[Map[KEY, VALUE], P2]#to[Boolean, R]) = {
    om.column(KebsHStoreLibrary.Contains, n, c2.toNode)
  }
  def <@:[P2, R](c2: Rep[P2])(implicit om: o#arg[Map[KEY, VALUE], P2]#to[Boolean, R]) = {
    om.column(KebsHStoreLibrary.ContainedBy, c2.toNode, n)
  }

  def @+[P2, R](c2: Rep[P2])(implicit om: o#arg[Map[KEY, VALUE], P2]#to[Map[KEY, VALUE], R]) = {
    om.column(KebsHStoreLibrary.Concatenate, n, c2.toNode)
  }
  def @-[P2, R](c2: Rep[P2])(implicit om: o#arg[Map[KEY, VALUE], P2]#to[Map[KEY, VALUE], R]) = {
    om.column(KebsHStoreLibrary.Delete, n, c2.toNode)
  }
  def --[P2, R](c2: Rep[P2])(implicit om: o#arg[List[KEY], P2]#to[Map[KEY, VALUE], R]) = {
    om.column(KebsHStoreLibrary.Delete, n, c2.toNode)
  }
  def -/[P2, R](c2: Rep[P2])(implicit om: o#arg[KEY, P2]#to[Map[KEY, VALUE], R]) = {
    om.column(KebsHStoreLibrary.Delete, n, c2.toNode)
  }
  def slice[P2, R](c2: Rep[P2])(implicit om: o#arg[List[KEY], P2]#to[Map[KEY, VALUE], R]) = {
    om.column(KebsHStoreLibrary.Slice, n, c2.toNode)
  }
}
