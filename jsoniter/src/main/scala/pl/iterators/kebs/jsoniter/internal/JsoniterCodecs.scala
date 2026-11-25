package pl.iterators.kebs.jsoniter.internal

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonCodec, JsonReader, JsonWriter}

import java.math.MathContext
import java.time._
import java.util.UUID

// of the standard types that jsoniter-scala can read and write; called by generated code
object JsoniterCodecs {
  private def primitive[A](empty: A)(read: JsonReader => A, write: (JsonWriter, A) => Unit)(
      readKey: JsonReader => A,
      writeKey: (JsonWriter, A) => Unit
  ): JsonCodec[A] = new JsonCodec[A] {
    def decodeValue(in: JsonReader, default: A): A = read(in)
    def encodeValue(x: A, out: JsonWriter): Unit   = write(out, x)
    def nullValue: A                               = empty
    def decodeKey(in: JsonReader): A               = readKey(in)
    def encodeKey(x: A, out: JsonWriter): Unit     = writeKey(out, x)
  }

  // jsoniter reads a null as the default
  private def reference[A <: AnyRef](read: JsonReader => A, write: (JsonWriter, A) => Unit)(
      readKey: JsonReader => A,
      writeKey: (JsonWriter, A) => Unit
  ): JsonCodec[A] = new JsonCodec[A] {
    def decodeValue(in: JsonReader, default: A): A = {
      val value = read(in)
      if (value eq null) in.decodeError("unexpected null")
      value
    }
    def encodeValue(x: A, out: JsonWriter): Unit = write(out, x)
    def nullValue: A                             = null.asInstanceOf[A]
    def decodeKey(in: JsonReader): A             = readKey(in)
    def encodeKey(x: A, out: JsonWriter): Unit   = writeKey(out, x)
  }

  val boolean: JsonCodec[Boolean] = primitive[Boolean](false)(_.readBoolean(), _.writeVal(_))(_.readKeyAsBoolean(), _.writeKey(_))
  val byte: JsonCodec[Byte]       = primitive[Byte](0)(_.readByte(), _.writeVal(_))(_.readKeyAsByte(), _.writeKey(_))
  val short: JsonCodec[Short]     = primitive[Short](0)(_.readShort(), _.writeVal(_))(_.readKeyAsShort(), _.writeKey(_))
  val int: JsonCodec[Int]         = primitive[Int](0)(_.readInt(), _.writeVal(_))(_.readKeyAsInt(), _.writeKey(_))
  val long: JsonCodec[Long]       = primitive[Long](0L)(_.readLong(), _.writeVal(_))(_.readKeyAsLong(), _.writeKey(_))
  val float: JsonCodec[Float]     = primitive[Float](0.0f)(_.readFloat(), _.writeVal(_))(_.readKeyAsFloat(), _.writeKey(_))
  val double: JsonCodec[Double]   = primitive[Double](0.0)(_.readDouble(), _.writeVal(_))(_.readKeyAsDouble(), _.writeKey(_))
  val char: JsonCodec[Char]       = primitive[Char]('\u0000')(_.readChar(), _.writeVal(_))(_.readKeyAsChar(), _.writeKey(_))

  val string: JsonCodec[String] = reference[String](_.readString(null), _.writeVal(_))(_.readKeyAsString(), _.writeKey(_))
  val bigInt: JsonCodec[BigInt] = reference[BigInt](_.readBigInt(null), _.writeVal(_))(_.readKeyAsBigInt(), _.writeKey(_))
  // jsoniter rounds to 34 digits unless it is told not to; its limits stay
  val bigDecimal: JsonCodec[BigDecimal] = reference[BigDecimal](
    _.readBigDecimal(null, MathContext.UNLIMITED, JsonReader.bigDecimalScaleLimit, JsonReader.bigDecimalDigitsLimit),
    _.writeVal(_)
  )(
    _.readKeyAsBigDecimal(MathContext.UNLIMITED, JsonReader.bigDecimalScaleLimit, JsonReader.bigDecimalDigitsLimit),
    _.writeKey(_)
  )
  val uuid: JsonCodec[UUID] = reference[UUID](_.readUUID(null), _.writeVal(_))(_.readKeyAsUUID(), _.writeKey(_))

  val duration: JsonCodec[Duration]   = reference[Duration](_.readDuration(null), _.writeVal(_))(_.readKeyAsDuration(), _.writeKey(_))
  val instant: JsonCodec[Instant]     = reference[Instant](_.readInstant(null), _.writeVal(_))(_.readKeyAsInstant(), _.writeKey(_))
  val localDate: JsonCodec[LocalDate] = reference[LocalDate](_.readLocalDate(null), _.writeVal(_))(_.readKeyAsLocalDate(), _.writeKey(_))
  val localDateTime: JsonCodec[LocalDateTime] =
    reference[LocalDateTime](_.readLocalDateTime(null), _.writeVal(_))(_.readKeyAsLocalDateTime(), _.writeKey(_))
  val localTime: JsonCodec[LocalTime] = reference[LocalTime](_.readLocalTime(null), _.writeVal(_))(_.readKeyAsLocalTime(), _.writeKey(_))
  val monthDay: JsonCodec[MonthDay]   = reference[MonthDay](_.readMonthDay(null), _.writeVal(_))(_.readKeyAsMonthDay(), _.writeKey(_))
  val offsetDateTime: JsonCodec[OffsetDateTime] =
    reference[OffsetDateTime](_.readOffsetDateTime(null), _.writeVal(_))(_.readKeyAsOffsetDateTime(), _.writeKey(_))
  val offsetTime: JsonCodec[OffsetTime] =
    reference[OffsetTime](_.readOffsetTime(null), _.writeVal(_))(_.readKeyAsOffsetTime(), _.writeKey(_))
  val period: JsonCodec[Period]       = reference[Period](_.readPeriod(null), _.writeVal(_))(_.readKeyAsPeriod(), _.writeKey(_))
  val year: JsonCodec[Year]           = reference[Year](_.readYear(null), _.writeVal(_))(_.readKeyAsYear(), _.writeKey(_))
  val yearMonth: JsonCodec[YearMonth] = reference[YearMonth](_.readYearMonth(null), _.writeVal(_))(_.readKeyAsYearMonth(), _.writeKey(_))
  val zonedDateTime: JsonCodec[ZonedDateTime] =
    reference[ZonedDateTime](_.readZonedDateTime(null), _.writeVal(_))(_.readKeyAsZonedDateTime(), _.writeKey(_))
  val zoneId: JsonCodec[ZoneId]         = reference[ZoneId](_.readZoneId(null), _.writeVal(_))(_.readKeyAsZoneId(), _.writeKey(_))
  val zoneOffset: JsonCodec[ZoneOffset] =
    reference[ZoneOffset](_.readZoneOffset(null), _.writeVal(_))(_.readKeyAsZoneOffset(), _.writeKey(_))
}
