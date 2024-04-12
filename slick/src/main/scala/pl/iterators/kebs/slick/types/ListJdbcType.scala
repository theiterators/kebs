package pl.iterators.kebs.slick.types

import slick.ast.{FieldSymbol, ScalaType}

import java.sql.{PreparedStatement, ResultSet}
import scala.reflect.ClassTag

class ListJdbcType[A] extends slick.jdbc.JdbcType[List[A]] {
  def sqlType: Int = java.sql.Types.ARRAY

  def setValue(v: List[A], p: PreparedStatement, idx: Int): Unit = p.setArray(idx, p.getConnection.createArrayOf("text", v.toArray))

  def getValue(r: ResultSet, idx: Int): List[A] = r.getArray(idx).getArray.asInstanceOf[List[A]]

  def updateValue(v: List[A], r: ResultSet, idx: Int): Unit = r.updateArray(idx, r.getStatement.getConnection.createArrayOf("text", v.toArray))

  override def hasLiteralForm: Boolean = false

  override def sqlTypeName(size: Option[FieldSymbol]): String = "text"

  override def setNull(p: PreparedStatement, idx: Int): Unit = p.setNull(idx, sqlType)

  override def wasNull(r: ResultSet, idx: Int): Boolean = r.wasNull()

  override def valueToSQLLiteral(value: List[A]): String = value.toString

  override def scalaType: ScalaType[List[A]] = null

  override def classTag: ClassTag[_] = ClassTag(classOf[List[A]])
}