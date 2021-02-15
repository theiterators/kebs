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

trait MinimalArbitrarySupport {
  implicit def emptyOption[T: Arbitrary]: Arbitrary[Option[T]] =
    Arbitrary(Gen.const(Option.empty[T]))

  implicit def emptySeq[T: Arbitrary]: Arbitrary[Seq[T]] =
    Arbitrary(Gen.const(Seq.empty[T]))

  implicit def emptyArray[T: Arbitrary: ClassTag]: Arbitrary[Array[T]] =
    Arbitrary(Gen.const(Array.empty[T]))

  implicit def emptySet[T: Arbitrary]: Arbitrary[Set[T]] =
    Arbitrary(Gen.const(Set.empty[T]))

  implicit def emptyVector[T: Arbitrary]: Arbitrary[Vector[T]] =
    Arbitrary(Gen.const(Vector.empty[T]))

  implicit def emptyList[T: Arbitrary]: Arbitrary[List[T]] =
    Arbitrary(Gen.const(List.empty[T]))

  implicit def emptyMap[T: Arbitrary, U: Arbitrary]: Arbitrary[Map[T, U]] =
    Arbitrary(Gen.const(Map.empty[T, U]))
}

trait MaximalArbitrarySupport {
  implicit def someOption[T: Arbitrary]: Arbitrary[Option[T]] =
    Arbitrary(Gen.some(Arbitrary.arbitrary[T]))

  implicit def nonEmptySeq[T: Arbitrary]: Arbitrary[Seq[T]] =
    Arbitrary(Gen.listOfN(1 + Random.nextInt(3), Arbitrary.arbitrary[T]))

  implicit def nonEmptyArray[T: Arbitrary: ClassTag]: Arbitrary[Array[T]] =
    Arbitrary(Gen.listOfN(1 + Random.nextInt(3), Arbitrary.arbitrary[T]).map(_.toArray))

  implicit def nonEmptySet[T: Arbitrary]: Arbitrary[Set[T]] =
    Arbitrary(Gen.listOfN(1 + Random.nextInt(3), Arbitrary.arbitrary[T]).map(_.toSet))

  implicit def nonEmptyVector[T: Arbitrary]: Arbitrary[Vector[T]] =
    Arbitrary(Gen.listOfN(1 + Random.nextInt(3), Arbitrary.arbitrary[T]).map(_.toVector))

  implicit def nonEmptyList[T: Arbitrary]: Arbitrary[List[T]] =
    Arbitrary(Gen.listOfN(1 + Random.nextInt(3), Arbitrary.arbitrary[T]))

  implicit def nonEmptyMap[T: Arbitrary, U: Arbitrary]: Arbitrary[Map[T, U]] =
    Arbitrary(Gen.mapOfN(1 + Random.nextInt(3), Arbitrary.arbitrary[(T, U)]))
}

trait KebsArbitraryPredefs {
  implicit val arbAlphaString: Arbitrary[String] =
    Arbitrary(Gen.alphaNumStr)

  implicit val arbInstant: Arbitrary[Instant] =
    Arbitrary(Gen.calendar.map(_.toInstant))

  implicit val arbLocalTime: Arbitrary[LocalTime] =
    Arbitrary(Gen.calendar.map(_.toInstant.atZone(ZoneId.systemDefault()).toLocalTime))

  implicit val arbLocalDate: Arbitrary[LocalDate] =
    Arbitrary(Gen.calendar.map(_.toInstant.atZone(ZoneId.systemDefault()).toLocalDate))

  implicit val arbLocalDateTime: Arbitrary[LocalDateTime] =
    Arbitrary(Gen.calendar.map(_.toInstant.atZone(ZoneId.systemDefault()).toLocalDateTime))

  implicit val arbZonedDataTime: Arbitrary[ZonedDateTime] =
    Arbitrary(Gen.calendar.map(_.toInstant.atZone(ZoneId.systemDefault())))

  implicit val arbDuration: Arbitrary[Duration] = Arbitrary(Gen.duration.map { duration =>
    if (!duration.isFinite) ChronoUnit.FOREVER.getDuration
    else if (duration.length == 0) Duration.ZERO
    else
      duration.unit match {
        case TimeUnit.NANOSECONDS  => Duration.ofNanos(duration.length)
        case TimeUnit.MICROSECONDS => Duration.of(duration.length, ChronoUnit.MICROS)
        case TimeUnit.MILLISECONDS => Duration.ofMillis(duration.length)
        case TimeUnit.SECONDS      => Duration.ofSeconds(duration.length)
        case TimeUnit.MINUTES      => Duration.ofMinutes(duration.length)
        case TimeUnit.HOURS        => Duration.ofHours(duration.length)
        case TimeUnit.DAYS         => Duration.ofDays(duration.length)
      }
  })

  implicit val arbUrl: Arbitrary[URL] = Arbitrary {
    for {
      protocol  <- Gen.oneOf("http", "https", "ftp", "file")
      domain    <- Gen.alphaNumStr
      subdomain <- Gen.alphaNumStr
      path      <- Gen.alphaNumStr
    } yield new URL(s"$protocol://$subdomain.$domain.test/$path")
  }

  implicit val arbUri: Arbitrary[URI] = Arbitrary(arbUrl.arbitrary.map(_.toURI))

}
