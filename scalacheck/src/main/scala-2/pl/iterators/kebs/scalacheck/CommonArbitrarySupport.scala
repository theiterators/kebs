package pl.iterators.kebs.scalacheck

import enumeratum.ScalacheckInstances
import org.scalacheck.{Arbitrary, Gen}
import pl.iterators.kebs.macros.CaseClass1Rep

import java.net.{URI, URL}
import java.time.temporal.ChronoUnit
import java.time._
import java.util.concurrent.TimeUnit
import scala.reflect.ClassTag
import scala.util.Random
import magnolify.scalacheck.auto._

trait CommonArbitrarySupport extends ScalacheckInstances {
  implicit def caseClass1RepArbitraryPredef[T, A](
      implicit rep: CaseClass1Rep[T, A],
      arb: Arbitrary[A]
  ): Arbitrary[T] = {
    Arbitrary(arb.arbitrary.map(rep.apply(_)))
  }
}