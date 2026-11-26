package pl.iterators.kebs.circe

import dotty.tools.dotc.interfaces.{Diagnostic, SimpleReporter}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.file.Files
import scala.collection.mutable.ListBuffer
import scala.io.Source

// typeCheckErrors does not see warnings, so the snippets go through the compiler
class CirceConfigurationWarningTests extends AnyFunSuite with Matchers {
  private val snippets = Map(
    "Local.scala" -> """
      import io.circe.{Decoder, Encoder}
      import io.circe.derivation.Configuration
      object Local {
        object P extends pl.iterators.kebs.circe.KebsCirce
        import P._
        implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
        final case class C(fooBar: Int)
        val e = implicitly[Encoder[C]]
        val d = implicitly[Decoder[C]]
      }""",
    "InProtocol.scala" -> """
      import io.circe.Encoder
      import io.circe.derivation.Configuration
      object InProtocol {
        object P extends pl.iterators.kebs.circe.KebsCirce {
          implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
        }
        import P._
        final case class C(fooBar: Int)
        val e = implicitly[Encoder[C]]
      }""",
    "Without.scala" -> """
      import io.circe.{Decoder, Encoder}
      object Without {
        object P extends pl.iterators.kebs.circe.KebsCirce
        import P._
        final case class C(fooBar: Int)
        val e = implicitly[Encoder[C]]
        val d = implicitly[Decoder[C]]
      }"""
  )

  private lazy val diagnostics: List[(Int, String, String)] = {
    val dir   = Files.createTempDirectory("kebs-circe-warning")
    val files = snippets.map { case (name, code) =>
      val file = dir.resolve(name)
      Files.writeString(file, code)
      file.toString
    }
    val classpath                = Source.fromResource("kebs-test-classpath.txt").mkString
    val found                    = ListBuffer.empty[(Int, String, String)]
    val reporter: SimpleReporter = (d: Diagnostic) =>
      found += ((d.level, if (d.position.isPresent) d.position.get.source.name else "", d.message))
    dotty.tools.dotc.Main.process(Array("-classpath", classpath, "-d", dir.toString) ++ files, reporter, null)
    found.toList
  }

  private def warnings(file: String) =
    diagnostics.collect { case (Diagnostic.WARNING, `file`, message) if message.contains("kebsJsonConfig") => message }

  test("the snippets compile") {
    diagnostics.filter(_._1 == Diagnostic.ERROR) shouldBe empty
  }

  test("an implicit circe Configuration next to derivation is reported, once per derived instance") {
    warnings("Local.scala") should have size 2
  }

  test("an implicit circe Configuration in the protocol is reported") {
    warnings("InProtocol.scala") should have size 1
  }

  test("nothing is reported without one") {
    warnings("Without.scala") shouldBe empty
  }
}
