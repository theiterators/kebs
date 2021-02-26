package pl.iterators.kebs.instances

import java.net.URISyntaxException
import java.time.DateTimeException

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

  case class DecodeError(msg: String, exception: Throwable)

  private[instances] def decodeObject[Obj, Val](decode: Val => Either[DecodeError, Obj])(value: Val): Obj = {
    decode(value) match {
      case Left(DecodeError(msg, e)) => throw new IllegalArgumentException(msg, e)
      case Right(value)              => value
    }
  }

  // TODO (25/02/21) better handling of specific exceptions, for example UnknownHostException and SecurityException for NetInstances?
  private[instances] def tryParse[Obj, Val](parse: Val => Obj, value: Val, clazz: Class[Obj], format: String): Either[DecodeError, Obj] = {
    try {
      Right(parse(value))
    } catch {
      case e: IllegalArgumentException => Left(DecodeError(errorMessage(clazz, value, format), e))
      case e: NullPointerException     => Left(DecodeError(errorMessage(clazz, value, format), e))
      case e: URISyntaxException       => Left(DecodeError(errorMessage(clazz, value, format), e))
      case e: DateTimeException        => Left(DecodeError(errorMessage(clazz, value, format), e))
      case e: Throwable                => throw e
    }
  }

  private[instances] def errorMessage[Obj, Val](clazz: Class[Obj], value: Val, format: String): String =
    s"${clazz.getName} cannot be parsed from $value – should be in format $format"
}
