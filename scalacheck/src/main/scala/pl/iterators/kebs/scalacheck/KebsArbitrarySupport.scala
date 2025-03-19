package pl.iterators.kebs.scalacheck

import org.scalacheck.{Arbitrary, Gen}
import pl.iterators.kebs.core.macros.ValueClassLike

import java.net.{URI, URL}
import java.time.temporal.ChronoUnit
import java.time._
import java.util.concurrent.TimeUnit

trait KebsArbitrarySupport extends ScalacheckInstancesSupport {
  implicit def valueClassLikeArbitraryPredef[T, A](implicit
      rep: ValueClassLike[T, A],
      arbitrary: Arbitrary[A]
  ): Arbitrary[T] =
    Arbitrary(arbitrary.arbitrary.map(rep.apply(_)))

  implicit val arbUnit: Arbitrary[Unit] = Arbitrary(Gen.const(()))

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

  implicit val arbUri: Arbitrary[URI] = Arbitrary {
    for {
      protocol  <- Gen.oneOf("http", "https", "ftp", "file")
      domain    <- Gen.alphaNumStr
      subdomain <- Gen.alphaNumStr
      path      <- Gen.alphaNumStr
    } yield new URI(s"$protocol://$subdomain.$domain.test/$path")
  }

  implicit val arbUrl: Arbitrary[URL] = Arbitrary(arbUri.arbitrary.map(_.toURL))

  implicit def arbSeq[T: Arbitrary]: Arbitrary[Seq[T]] = Arbitrary {
    Gen.listOf(Arbitrary.arbitrary[T]).map(_.toSeq)
  }
}

object KebsArbitrarySupport extends KebsArbitrarySupport
