package pl.iterators.kebs.scalacheck

import org.scalacheck.{Arbitrary, Gen}
import pl.iterators.kebs.core.macros.ValueClassLike

import java.net.{URI, URL}
import java.time.temporal.ChronoUnit
import java.time._
import java.util.concurrent.TimeUnit
import scala.reflect.ClassTag
import scala.util.Random
import io.github.martinhh.derived.scalacheck.given
import enumeratum.ScalacheckInstances

trait CommonArbitrarySupport extends ScalacheckInstances {
  implicit def ValueClassLikeArbitraryPredef[T, A](
                                                   implicit rep: ValueClassLike[T, A],
                                                   arbitrary: Arbitrary[A]
  ): Arbitrary[T] =
    Arbitrary(arbitrary.arbitrary.map(rep.apply(_)))
}