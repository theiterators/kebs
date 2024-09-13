package pl.iterators.kebs.pureconfig.caseclasses

import com.typesafe.config.ConfigFactory
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.macros.CaseClass1ToValueClass
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter}

case class MyConfig(value: ValueLike, other: String)
case class ValueLike(value: Int)

class PureConfigTests extends AnyFunSuite with Matchers with CaseClass1ToValueClass {
  import pureconfig.generic.semiauto._
  import pl.iterators.kebs.pureconfig._

  test("Derive ConfigReader for case class 1") {
    implicit val myConfigReader: ConfigReader[MyConfig] = deriveReader[MyConfig]
    val config                                          = ConfigFactory.parseString("value = 5, other = test")
    val myConfig                                        = ConfigSource.fromConfig(config).load[MyConfig]
    myConfig shouldBe Right(MyConfig(ValueLike(5), "test"))
  }

  test("Derive ConfigWriter for case class 1") {
    implicit val myConfigWriter: ConfigWriter[MyConfig] = deriveWriter[MyConfig]
    val myConfig                                        = MyConfig(ValueLike(5), "test")
    val config                                          = myConfigWriter.to(myConfig)
    config shouldBe ConfigFactory.parseString("value = 5, other = test").root()
  }
}
