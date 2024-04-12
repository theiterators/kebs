package pl.iterators.kebs.slick.types

import slick.ast.{FieldSymbol, ScalaType}

import java.sql.{PreparedStatement, ResultSet}
import scala.reflect.ClassTag

class GenericJdbcType[T](val sqlTypeName: String,
                         fnFromString: (String => T),
                         fnToString: (T => String) = ((r: T) => r.toString),
                         val sqlType: Int = java.sql.Types.OTHER,
                         override val hasLiteralForm: Boolean = false)(
                          implicit override val classTag: ClassTag[T]) extends slick.jdbc.JdbcType[T] {

  override def sqlTypeName(sym: Option[FieldSymbol]): String = sqlTypeName

  override def getValue(r: ResultSet, idx: Int): T = {
    val value = r.getString(idx)
    if (r.wasNull) null.asInstanceOf[T] else fnFromString(value)
  }

  override def setValue(v: T, p: PreparedStatement, idx: Int): Unit = p.setObject(idx, toStr(v), java.sql.Types.OTHER)

  override def updateValue(v: T, r: ResultSet, idx: Int): Unit = r.updateObject(idx, toStr(v), java.sql.Types.OTHER)

  override def valueToSQLLiteral(v: T) = if(v == null) "NULL" else s"'${fnToString(v)}'"

  private def toStr(v: T) = if(v == null) null else fnToString(v)

  override def setNull(p: PreparedStatement, idx: Int): Unit = p.setNull(idx, sqlType)

  override def wasNull(r: ResultSet, idx: Int): Boolean = r.wasNull()

  override def scalaType: ScalaType[T] = null
}

