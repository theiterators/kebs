package pl.iterators.kebs.tagged.circe

import java.util.UUID

import io.circe.Decoder.Result
import io.circe.{Codec, Decoder, Encoder, HCursor, Json}
import pl.iterators.kebs.tagged.@@
import io.circe.syntax._
import pl.iterators.kebs.tagged._

trait KebsTaggedCirce {

  private def taggedCodec[U: Decoder: Encoder, T]: Codec[U @@ T] =
    new Codec[U @@ T] {
      override def apply(c: HCursor): Result[U @@ T] =
        Predef.implicitly[Decoder[U]].apply(c).map(u => u.taggedWith[T])

      override def apply(a: U @@ T): Json = a.asInstanceOf[U].asJson
    }

  implicit def taggedStringCodec[T](implicit d: Decoder[String], e: Encoder[String]): Codec[String @@ T] =
    taggedCodec[String, T]

  implicit def taggedUUIDCodec[T](implicit d: Decoder[UUID], e: Encoder[UUID]): Codec[UUID @@ T] =
    taggedCodec[UUID, T]

  implicit def taggedBigDecimalCodec[T](implicit d: Decoder[BigDecimal], e: Encoder[BigDecimal]): Codec[BigDecimal @@ T] =
    taggedCodec[BigDecimal, T]

  implicit def taggedIntCodec[T](implicit d: Decoder[Int], e: Encoder[Int]): Codec[Int @@ T] =
    taggedCodec[Int, T]

  implicit def taggedJsonCodec[T](implicit d: Decoder[Json], e: Encoder[Json]): Codec[Json @@ T] =
    taggedCodec[Json, T]

}
