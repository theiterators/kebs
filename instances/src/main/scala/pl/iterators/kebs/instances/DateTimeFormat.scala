package pl.iterators.kebs.instances

import java.time.format.DateTimeFormatter

trait DateTimeFormat {

  implicit val localDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  implicit val localDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  implicit val localTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

  implicit val offsetDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  implicit val offsetTimeFormatter = DateTimeFormatter.ISO_OFFSET_TIME

  implicit val zonedDateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

}
