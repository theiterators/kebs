package pl.iterators.kebs.scalacheck

import enumeratum.ScalacheckInstances
import org.scalacheck.{Arbitrary, Gen, ScalacheckShapeless}
import pl.iterators.kebs.macros.CaseClass1Rep

import java.net.{URI, URL}
import java.time.temporal.ChronoUnit
import java.time._
import java.util.concurrent.TimeUnit
import scala.reflect.ClassTag
import scala.util.Random

trait CommonArbitrarySupport extends ScalacheckShapeless with ScalacheckInstances {
  implicit def caseClass1RepArbitraryPredef[T, A](
      implicit rep: CaseClass1Rep[T, A],
      arbitrary: Arbitrary[A]
  ): Arbitrary[T] =
    Arbitrary(arbitrary.arbitrary.map(rep.apply(_)))
}