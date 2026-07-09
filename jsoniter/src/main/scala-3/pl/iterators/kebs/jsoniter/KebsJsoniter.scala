package pl.iterators.kebs.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import pl.iterators.kebs.core.instances.InstanceConverter
import pl.iterators.kebs.core.macros.ValueClassLike

import java.time.{Duration => JDuration, _}
import java.util.UUID
import scala.annotation.nowarn
import scala.compiletime.{error, summonFrom}
import scala.concurrent.duration.FiniteDuration
import scala.util.{NotGiven, Try}

private[jsoniter] object KebsJsoniterCodecs {
  def valueClassCodec[T, A](rep: ValueClassLike[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    new JsonValueCodec[T] {
      override def decodeValue(in: JsonReader, default: T): T = {
        val d: A = Try(rep.unapply(default)).toOption.getOrElse(null.asInstanceOf[A])
        rep.apply(codecA.decodeValue(in, d))
      }
      override def encodeValue(x: T, out: JsonWriter): Unit = codecA.encodeValue(rep.unapply(x), out)
      override val nullValue: T                             = Try(rep.apply(codecA.nullValue)).toOption.getOrElse(null.asInstanceOf[T])
    }

  def instanceConverterCodec[T, A](rep: InstanceConverter[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    new JsonValueCodec[T] {
      override def decodeValue(in: JsonReader, default: T): T = {
        val d: A = Try(rep.encode(default)).toOption.getOrElse(null.asInstanceOf[A])
        rep.decode(codecA.decodeValue(in, d))
      }
      override def encodeValue(x: T, out: JsonWriter): Unit = codecA.encodeValue(rep.encode(x), out)
      override val nullValue: T                             = Try(rep.decode(codecA.nullValue)).toOption.getOrElse(null.asInstanceOf[T])
    }
}

/** Codecs for the types that commonly underlie kebs value classes / opaque types. They are imported only inside `deriveCodec`'s expansion
  * and are deliberately NOT part of any mixin trait: an implicit `JsonValueCodec[String]` visible in route scope would make a jsoniter
  * marshaller hijack pekko's `text/plain` handling of plain Strings.
  */
private[jsoniter] object KebsJsoniterBaseCodecs {
  private def nullSafe[A <: AnyRef](underlying: JsonValueCodec[A]): JsonValueCodec[A] =
    new JsonValueCodec[A] {
      override def decodeValue(in: JsonReader, default: A): A = underlying.decodeValue(in, default)
      override def encodeValue(x: A, out: JsonWriter): Unit   = if (x == null) out.writeNull() else underlying.encodeValue(x, out)
      override def nullValue: A                               = underlying.nullValue
    }

  implicit val stringBaseCodec: JsonValueCodec[String]                 = nullSafe(JsonCodecMaker.make)
  implicit val intBaseCodec: JsonValueCodec[Int]                       = JsonCodecMaker.make
  implicit val longBaseCodec: JsonValueCodec[Long]                     = JsonCodecMaker.make
  implicit val doubleBaseCodec: JsonValueCodec[Double]                 = JsonCodecMaker.make
  implicit val floatBaseCodec: JsonValueCodec[Float]                   = JsonCodecMaker.make
  implicit val shortBaseCodec: JsonValueCodec[Short]                   = JsonCodecMaker.make
  implicit val byteBaseCodec: JsonValueCodec[Byte]                     = JsonCodecMaker.make
  implicit val booleanBaseCodec: JsonValueCodec[Boolean]               = JsonCodecMaker.make
  implicit val bigDecimalBaseCodec: JsonValueCodec[BigDecimal]         = nullSafe(JsonCodecMaker.make)
  implicit val bigIntBaseCodec: JsonValueCodec[BigInt]                 = nullSafe(JsonCodecMaker.make)
  implicit val uuidBaseCodec: JsonValueCodec[UUID]                     = nullSafe(JsonCodecMaker.make)
  implicit val instantBaseCodec: JsonValueCodec[Instant]               = nullSafe(JsonCodecMaker.make)
  implicit val localDateBaseCodec: JsonValueCodec[LocalDate]           = nullSafe(JsonCodecMaker.make)
  implicit val localDateTimeBaseCodec: JsonValueCodec[LocalDateTime]   = nullSafe(JsonCodecMaker.make)
  implicit val localTimeBaseCodec: JsonValueCodec[LocalTime]           = nullSafe(JsonCodecMaker.make)
  implicit val offsetDateTimeBaseCodec: JsonValueCodec[OffsetDateTime] = nullSafe(JsonCodecMaker.make)
  implicit val zonedDateTimeBaseCodec: JsonValueCodec[ZonedDateTime]   = nullSafe(JsonCodecMaker.make)
  implicit val durationBaseCodec: JsonValueCodec[JDuration]            = nullSafe(JsonCodecMaker.make)
  implicit val finiteDurationBaseCodec: JsonValueCodec[FiniteDuration] = nullSafe(JsonCodecMaker.make)
}

/** Shape-specific instances: they require ValueClassLike / InstanceConverter evidence, so they always pin the type variable during
  * implicit search and cannot trigger the contravariant-Marshaller Any-maximization.
  */
private[jsoniter] trait KebsJsoniterValueCodecs {
  implicit def flatCodec[T, A](using rep: ValueClassLike[T, A], codecA: JsonValueCodec[A]): JsonValueCodec[T] =
    KebsJsoniterCodecs.valueClassCodec(rep, codecA)
  implicit def instanceConverterCodec[T, A](using
      rep: InstanceConverter[T, A],
      codecA: JsonValueCodec[A],
      ev: NotGiven[T =:= A]
  ): JsonValueCodec[T] =
    KebsJsoniterCodecs.instanceConverterCodec(rep, codecA)
}

/** Scala 3 note — why `deriveCodec` is explicit here while it is an implicit macro on Scala 2:
  *
  * On Scala 2, two mechanisms make a universal implicit fallback safe: (1) scalac pins the contravariant type parameter of pekko's
  * `Marshaller[-A, _]` to the expected type, and (2) a blackbox implicit macro that fails to expand silently withdraws from the search.
  * Scala 3 has neither: an unpinned contravariant type variable is maximized to `Any`, and a selected inline given that fails to expand is
  * a hard error. A universal `implicit inline def exportCodec[A]` therefore breaks EVERY pekko marshalling site ("Please consider sealing
  * the 'scala.Any' ...") and cannot be guarded after selection.
  *
  * Instead, wire types declare their codec once, in the companion object:
  * {{{
  * final case class Foo(id: FooId, name: FooName)
  * object Foo {
  *   implicit val codec: JsonValueCodec[Foo]         = deriveCodec
  *   implicit val seqCodec: JsonValueCodec[Seq[Foo]] = deriveCodec // jsoniter codecs are whole-value: derive the exact wire shape
  * }
  * }}}
  *
  * The implicits declared inside the inline body bridge kebs value classes / opaque types and their underlying primitives for the
  * derivation macro's nested lookups; being local to the expansion they can never leak into marshaller resolution at the call site.
  */
trait KebsJsoniter extends KebsJsoniterValueCodecs {
  @nowarn("msg=unused")
  inline def deriveCodec[A]: JsonValueCodec[A] =
    summonFrom {
      case _: ValueClassLike[A, t] =>
        error(
          "this is a kebs value-class/opaque type: summon[JsonValueCodec[A]] uses flatCodec for it; derive the enclosing wire type instead"
        )
      case _: InstanceConverter[A, t] =>
        error(
          "this is a kebs instance-converted type: summon[JsonValueCodec[A]] uses instanceConverterCodec for it; derive the enclosing wire type instead"
        )
      case _ =>
        implicit def kebsValueClassCodec[T, B](implicit rep: ValueClassLike[T, B], codecB: JsonValueCodec[B]): JsonValueCodec[T] =
          KebsJsoniterCodecs.valueClassCodec(rep, codecB)
        implicit def kebsInstanceConverterCodec[T, B](implicit
            rep: InstanceConverter[T, B],
            codecB: JsonValueCodec[B],
            ev: NotGiven[T =:= B]
        ): JsonValueCodec[T] =
          KebsJsoniterCodecs.instanceConverterCodec(rep, codecB)
        import KebsJsoniterBaseCodecs._
        // config repeated literally in each flavor: JsonCodecMaker.make requires a compile-time-constant config expression
        JsonCodecMaker.make[A](
          CodecMakerConfig
            .withAllowRecursiveTypes(true)
            .withTransientEmpty(false)
            .withTransientNull(false)
            .withTransientNone(false)
        )
    }
}

trait KebsJsoniterSnakified extends KebsJsoniterValueCodecs {
  @nowarn("msg=unused")
  inline def deriveCodec[A]: JsonValueCodec[A] =
    summonFrom {
      case _: ValueClassLike[A, t] =>
        error(
          "this is a kebs value-class/opaque type: summon[JsonValueCodec[A]] uses flatCodec for it; derive the enclosing wire type instead"
        )
      case _: InstanceConverter[A, t] =>
        error(
          "this is a kebs instance-converted type: summon[JsonValueCodec[A]] uses instanceConverterCodec for it; derive the enclosing wire type instead"
        )
      case _ =>
        implicit def kebsValueClassCodec[T, B](implicit rep: ValueClassLike[T, B], codecB: JsonValueCodec[B]): JsonValueCodec[T] =
          KebsJsoniterCodecs.valueClassCodec(rep, codecB)
        implicit def kebsInstanceConverterCodec[T, B](implicit
            rep: InstanceConverter[T, B],
            codecB: JsonValueCodec[B],
            ev: NotGiven[T =:= B]
        ): JsonValueCodec[T] =
          KebsJsoniterCodecs.instanceConverterCodec(rep, codecB)
        import KebsJsoniterBaseCodecs._
        JsonCodecMaker.make[A](
          CodecMakerConfig
            .withAllowRecursiveTypes(true)
            .withTransientEmpty(false)
            .withTransientNull(false)
            .withTransientNone(false)
            .withAdtLeafClassNameMapper(x => JsonCodecMaker.enforce_snake_case(JsonCodecMaker.simpleClassName(x)))
            .withFieldNameMapper(JsonCodecMaker.enforce_snake_case)
        )
    }
}

trait KebsJsoniterCapitalized extends KebsJsoniterValueCodecs {
  @nowarn("msg=unused")
  inline def deriveCodec[A]: JsonValueCodec[A] =
    summonFrom {
      case _: ValueClassLike[A, t] =>
        error(
          "this is a kebs value-class/opaque type: summon[JsonValueCodec[A]] uses flatCodec for it; derive the enclosing wire type instead"
        )
      case _: InstanceConverter[A, t] =>
        error(
          "this is a kebs instance-converted type: summon[JsonValueCodec[A]] uses instanceConverterCodec for it; derive the enclosing wire type instead"
        )
      case _ =>
        implicit def kebsValueClassCodec[T, B](implicit rep: ValueClassLike[T, B], codecB: JsonValueCodec[B]): JsonValueCodec[T] =
          KebsJsoniterCodecs.valueClassCodec(rep, codecB)
        implicit def kebsInstanceConverterCodec[T, B](implicit
            rep: InstanceConverter[T, B],
            codecB: JsonValueCodec[B],
            ev: NotGiven[T =:= B]
        ): JsonValueCodec[T] =
          KebsJsoniterCodecs.instanceConverterCodec(rep, codecB)
        import KebsJsoniterBaseCodecs._
        JsonCodecMaker.make[A](
          CodecMakerConfig
            .withAllowRecursiveTypes(true)
            .withTransientEmpty(false)
            .withTransientNull(false)
            .withTransientNone(false)
            .withFieldNameMapper(JsonCodecMaker.EnforcePascalCase)
        )
    }
}
