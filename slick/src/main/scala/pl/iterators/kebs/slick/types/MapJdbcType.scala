package pl.iterators.kebs.slick.types

import slick.ast.{FieldSymbol, ScalaType}

import java.sql.{PreparedStatement, ResultSet}
import scala.reflect.ClassTag

class MapJdbcType[A, B] extends slick.jdbc.JdbcType[Map[A, B]] {
  def sqlType: Int = java.sql.Types.LONGVARCHAR

  def setValue(v: Map[A, B], p: PreparedStatement, idx: Int): Unit = p.setArray(idx, p.getConnection.createArrayOf("text", v.map { case (k, v) => s"$k:$v" }.toArray))

  def getValue(r: ResultSet, idx: Int): Map[A, B] = {
    val data = r.getString(idx).split(",")
    data.map { s =>
      val keyValue = s.split(":")
      if (keyValue.length == 2)
        keyValue(0).asInstanceOf[A] -> keyValue(1).asInstanceOf[B]
      else
        throw new IllegalArgumentException("Invalid format")
    }.toMap
  }

  def updateValue(v: Map[A, B], r: ResultSet, idx: Int): Unit = r.updateString(idx, v.map { case (k, v) => s"$k:$v" }.mkString(","))

  override def hasLiteralForm = false

  override def sqlTypeName(size: Option[FieldSymbol]): String = "text"

  override def setNull(p: PreparedStatement, idx: Int): Unit = p.setNull(idx, sqlType)

  override def wasNull(r: ResultSet, idx: Int): Boolean = r.wasNull()

  override def valueToSQLLiteral(value: Map[A, B]): String = value.map { case (k, v) => s"$k:$v" }.mkString(",")

  override def scalaType: ScalaType[Map[A, B]] = null

  override def classTag: ClassTag[_] = ClassTag(classOf[Map[A, B]])
}
