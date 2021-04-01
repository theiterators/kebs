package pl.iterators.kebs.instances

import scala.util.control.NonFatal

trait Instances {

  trait InstancesFormatter[Obj, Val] {
    def encode(obj: Obj): Val
    def decode(value: Val): Either[DecodeError, Obj]
  }
  object InstancesFormatter {
    def apply[Obj, Val](_encode: Obj => Val, _decode: Val => Either[DecodeError, Obj]): InstancesFormatter[Obj, Val] =
      new InstancesFormatter[Obj, Val] {
        override def decode(value: Val): Either[DecodeError, Obj] = _decode(value)
        override def encode(obj: Obj): Val                        = _encode(obj)
      }
  }

  case class DecodeError(msg: String, exception: Option[Throwable])

  private[instances] def decodeObject[Obj, Val](decode: Val => Either[DecodeError, Obj])(value: Val): Obj =
    decode(value) match {
      case Left(DecodeError(msg, Some(e))) => throw new IllegalArgumentException(msg, e)
      case Left(DecodeError(msg, None))    => throw new IllegalArgumentException(msg)
      case Right(value)                    => value
    }

  private[instances] def tryDecode[Obj, Val](decode: Val => Obj, value: Val, clazz: Class[Obj], format: String): Either[DecodeError, Obj] =
    try {
      Right(decode(value))
    } catch {
      case NonFatal(e) => Left(DecodeError(errorMessage(clazz, value, format), Some(e)))
    }

  private[instances] def errorMessage[Obj, Val](clazz: Class[Obj], value: Val, format: String): String =
    s"${clazz.getName} cannot be parsed from $value – should be in format $format"
}
