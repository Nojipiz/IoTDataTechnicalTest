//> using scala 3.4.1
import scala.concurrent.duration._
import scala.concurrent.Future
import java.time.format.*
import java.time.*
import scala.util.*
import java.text.SimpleDateFormat

import scala.concurrent.ExecutionContext.Implicits.global

private val outputTimeFormatter =
  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
private lazy val formatterMillisecondsTimezone =
  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
private lazy val formatterMilliseconds = DateTimeFormatter
  .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
  .withZone(ZoneId.of("UTC"))
private lazy val formatterSeconds = DateTimeFormatter
  .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
  .withZone(ZoneId.of("UTC"))

private val availableFormatters = Map(
  "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z" -> formatWith(
    formatterMillisecondsTimezone
  ),
  "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}" -> formatWith(
    formatterMilliseconds
  ),
  "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}" -> formatWith(formatterSeconds),
  "\\b\\d{1,11}\\b" -> formatEpochSeconds,
  "\\b\\d{12,}\\b" -> formatEpochMilis
)

@main
def run() = {
  println("Starting..")

  List(
    "'2021-12-03T16:15:30.235Z'",
    "'2021-12-03T16:15:30.235'",
    "'2011-12-03T10:15:30'",
    "'2021-10-28T00:00:00.000'",
    "1726668850124",
    "'1726668850124'",
    "1726667942",
    "'1726667942'",
    "969286895000",
    "'969286895000'",
    "3336042095",
    "'3336042095'",
    "3336042095000",
    "'3336042095000'"
  ).foreach(timestampWithUnknownFormat =>
    formatTimestamp(timestampWithUnknownFormat)
      .foreach(result => {
        println(
          timestampWithUnknownFormat + " -> " + result.fold(
            _.getMessage,
            outputTimeFormatter.format.apply
          )
        )
      })
  )

  Thread.sleep(2000) // Keeping JVM alive due Future's current implementation
}

def formatTimestamp(timestamp: String) = {
  lazy val cleanedTimestamp = timestamp.replace("\"", "").replace("'", "")

  val possibleFormatters = availableFormatters.map((regex, parseFunction) => {
    Future {
      Option.when(cleanedTimestamp.matches(regex))(parseFunction)
    }
  })

  val firstSuccessfullMatch =
    Future.find(possibleFormatters)(_.isDefined).map { formatter =>
      formatter.flatten
        .toRight(
          Throwable(s"No formatter found for timestamp ${cleanedTimestamp}")
        )
        .flatMap(_.apply(cleanedTimestamp))
    }

  firstSuccessfullMatch
}

private def formatWith(formatter: DateTimeFormatter)(timestamp: String) = Try {
  ZonedDateTime.parse(timestamp, formatter)
}.toEither

private def formatEpochMilis(timestamp: String) = timestamp.toLongOption match {
  case Some(time: Long) =>
    Try {
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC)
    }.toEither
  case None =>
    Left(Throwable("Invalid string to number convertion at millis convertion"))
}

private def formatEpochSeconds(timestamp: String) =
  timestamp.toLongOption match {
    case Some(time: Long) =>
      Try {
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneOffset.UTC)
      }.toEither
    case None =>
      Left(
        Throwable("Invalid string to number convertion at second convertion")
      )
  }
