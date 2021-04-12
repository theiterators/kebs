package pl.iterators.kebs.hstore

import com.github.tminglei.slickpg.ExPostgresProfile
import com.github.tminglei.slickpg.utils.{PgCommonJdbcTypes, PlainSQLUtils}
import org.postgresql.util.HStoreConverter
import slick.jdbc.{JdbcType, PositionedResult, PostgresProfile}

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.reflect.classTag

trait KebsPgHStoreSupport extends KebsPgHStoreExtensions with PgCommonJdbcTypes { driver: PostgresProfile =>
  import driver.api._

  trait HStoreCodeGenSupport {
    // register types to let `ExModelBuilder` find them
    if (driver.isInstanceOf[ExPostgresProfile]) {
      driver.asInstanceOf[ExPostgresProfile].bindPgTypeToScala("hstore", classTag[Map[String, String]])
    }
  }

  /// alias
  trait HStoreImplicits extends SimpleHStoreImplicits

  trait SimpleHStoreImplicits extends HStoreCodeGenSupport {
    implicit val simpleHStoreTypeMapper: JdbcType[Map[String, String]] =
      new GenericJdbcType[Map[String, String]](
        "hstore",
        (v) => HStoreConverter.fromString(v).asScala.toMap,
        (v) => HStoreConverter.toString(v.asJava),
        hasLiteralForm = false
      )

    // TODO define correct implicits
    implicit def simpleHStoreColumnExtensionMethods[B0, B1, SEQ[B1], MAP[B0, B1]](
        c: Rep[MAP[B0, B1]])(implicit t0: JdbcType[B0], t1: JdbcType[B1], tm: JdbcType[MAP[B0, B1]], ts: JdbcType[SEQ[B1]]) = {
      new HStoreColumnExtensionMethods[B0, B1, SEQ, MAP, MAP[B0, B1]](c)
    }

    implicit def simpleHStoreOptionColumnExtensionMethods[B0, B1, SEQ[B1], MAP[B0, B1]](
        c: Rep[Option[MAP[B0, B1]]])(implicit t0: JdbcType[B0], t1: JdbcType[B1], tm: JdbcType[MAP[B0, B1]], ts: JdbcType[SEQ[B1]]) = {
      new HStoreColumnExtensionMethods[B0, B1, SEQ, MAP, Option[MAP[B0, B1]]](c)
    }

  }

  /// static sql support, NOTE: no extension methods available for static sql usage
  trait SimpleHStorePlainImplicits extends HStoreCodeGenSupport {
    import PlainSQLUtils._

    implicit class PgHStorePositionedResult(r: PositionedResult) {
      def nextHStore() = nextHStoreOption().getOrElse(Map.empty)
      def nextHStoreOption() = r.nextStringOption().map { v =>
        HStoreConverter.fromString(v).asInstanceOf[java.util.Map[String, String]].asScala.toMap
      }
    }

    ////////////////////////////////////////////////////////////////////////
    implicit val getHStore       = mkGetResult(_.nextHStore())
    implicit val getHStoreOption = mkGetResult(_.nextHStoreOption())
    implicit val setHStore       = mkSetParameter[Map[String, String]]("hstore", (v) => HStoreConverter.toString(v.asJava))
    implicit val setHStoreOption = mkOptionSetParameter[Map[String, String]]("hstore", (v) => HStoreConverter.toString(v.asJava))
  }
}
