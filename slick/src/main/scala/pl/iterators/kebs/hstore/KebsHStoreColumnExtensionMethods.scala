package pl.iterators.kebs.hstore

import pl.iterators.kebs.macros.CaseClass1Rep
import slick.ast.Library._
import slick.ast.ScalaBaseType.booleanType
import slick.ast.TypedType
import slick.jdbc.JdbcType
import slick.lifted.{ExtensionMethods, Rep}

import java.time.YearMonth

/** Extension methods for hstore Columns */
class KebsHStoreColumnExtensionMethods[P1](val c: Rep[P1])(
    implicit tm: JdbcType[Map[YearMonth, String]],
    tl: JdbcType[List[YearMonth]],
    tl1: JdbcType[List[String]],
    ti: CaseClass1Rep[YearMonth, String]
) extends ExtensionMethods[Map[YearMonth, String], P1] {

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

  protected implicit def b1Type: TypedType[Map[YearMonth, String]] = implicitly[TypedType[Map[YearMonth, String]]]

  def ??[P2, R](k: Rep[P2])(implicit om: o#arg[YearMonth, P2]#to[Boolean, R]) = {
    om.column(Exist, n, k.toNode)
  }
  /*
    def +>[P2, R](k: Rep[P2])(implicit om: o#arg[String, P2]#to[String, R]) = {
        HStoreLibrary.On.column[Option[String]](n, k.toNode)
    }

    def >>[T: JdbcType](k: Rep[String]) = {
      Library.Cast.column[T](On.column[String](n, k.toNode).toNode)
    }
    def ??[P2, R](k: Rep[P2])(implicit om: o#arg[String, P2]#to[Boolean, R]) = {
      om.column(Exist, n, k.toNode)
    }
    def ?*[P2, R](k: Rep[P2])(implicit om: o#arg[String, P2]#to[Boolean, R]) = {
      om.column(Defined, n, k.toNode)
    }
    def ?|[P2, R](k: Rep[P2])(implicit om: o#arg[List[String], P2]#to[Boolean, R]) = {
      om.column(ExistAny, n, k.toNode)
    }
    def ?&[P2, R](k: Rep[P2])(implicit om: o#arg[List[String], P2]#to[Boolean, R]) = {
      om.column(ExistAll, n, k.toNode)
    }
    def @>[P2, R](c2: Rep[P2])(implicit om: o#arg[Map[String, String], P2]#to[Boolean, R]) = {
      om.column(Contains, n, c2.toNode)
    }
    def <@:[P2, R](c2: Rep[P2])(implicit om: o#arg[Map[String, String], P2]#to[Boolean, R]) = {
      om.column(ContainedBy, c2.toNode, n)
    }

    def @+[P2, R](c2: Rep[P2])(implicit om: o#arg[Map[String, String], P2]#to[Map[String, String], R]) = {
      om.column(Concatenate, n, c2.toNode)
    }
    def @-[P2, R](c2: Rep[P2])(implicit om: o#arg[Map[String, String], P2]#to[Map[String, String], R]) = {
      om.column(Delete, n, c2.toNode)
    }
    def --[P2, R](c2: Rep[P2])(implicit om: o#arg[List[String], P2]#to[Map[String, String], R]) = {
      om.column(Delete, n, c2.toNode)
    }
    def -/[P2, R](c2: Rep[P2])(implicit om: o#arg[String, P2]#to[Map[String, String], R]) = {
      om.column(Delete, n, c2.toNode)
    }
    def slice[P2, R](c2: Rep[P2])(implicit om: o#arg[List[String], P2]#to[Map[String, String], R]) = {
      om.column(Slice, n, c2.toNode)
    }*/
}
