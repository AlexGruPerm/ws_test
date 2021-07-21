/*
import scala.concurrent.duration.{DurationInt, FiniteDuration}
val dur: FiniteDuration = 30.seconds
_  <- IO.sleep(10.seconds)
*/

import java.time.{Instant, ZoneOffset}

import cats.effect.{Blocker, ContextShift, IO, Timer}
import java.util.concurrent._

import io.circe.parser
import org.http4s.{Header, Headers, HttpVersion, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._
import io.circe.Decoder

import scala.concurrent.ExecutionContext.global
import org.http4s.client.middleware.GZip

import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Date

object CommonFuncs {

  def zo = ZoneOffset.ofHours(+5)

  def convertLongToDate(l: Long): Date = new Date(l)

  //http://tutorials.jenkov.com/java-internationalization/simpledateformat.html
  // Pattern Syntax
  val DATE_FORMAT = "dd.MM.yyyy HH:mm:ss"

  /**
   * When we convert unix_timestamp to String representation of date and time is using same TimeZone.
   * Later we can adjust it with :
   *
   * val format = new SimpleDateFormat()
   * format.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"))
   * val dateAsString = format.format(date)
   *
   */
  def getDateAsString(d: Date): String = {
    val dateFormat = new SimpleDateFormat(DATE_FORMAT)
    dateFormat.format(d)
  }

}

object FonbetObject {

  case class Cell(
                   caption: Option[String],
                   isTitle: Option[Boolean],
                   captionAlign: Option[String],
                   factorId: Option[Int],
                   eventId: Option[Long],
                   value: Option[Double],
                   cartEventName: Option[String],
                   cartEventNameParametered: Option[String],
                   cartQuoteName: Option[String],
                   cartQuoteNameParametered: Option[String]
                 )

  case class Row(
                  isTitle: Option[Boolean],
                  cells: List[Cell]
                )

  case class Market(
                     marketId: Int,
                     ident: String,
                     sortOrder: Int,
                     caption: String,
                     commonHeaders: List[Int],
                     rows: List[Row]
                   )

  case class Event(
                    id: Long,
                    num: Int,
                    startTimeTimestamp: Long,
                    competitionId: Option[Int],
                    competitionName: Option[String],
                    competitionCaption: Option[String],
                    competitionsGroupId: Option[Int],
                    competitionsGroupCaption: Option[String],
                    skId: Int,
                    skName: String,
                    skSortOrder: String,
                    regionId: Option[Int],
                    team1Id: Int,
                    team2Id: Int,
                    team1: String,
                    team2: String,
                    statisticsType: String,
                    eventName: String,
                    name: String,
                    place: String,
                    priority: Int,
                    kind: Int,
                    rootKind: Int,
                    sortOrder: String,
                    sportViewId: Int,
                    allFactorsCount: Int,
                    markets: Option[List[Market]]
                  )
  case class FonbetLine(result: String,
                        request: String, place: String, lang: String,
                        events: List[Event])

  case class TestData(result: String)

}



object FonbetLineDecoders {
  import FonbetObject._

  implicit val decoderCell: Decoder[Cell] = Decoder.instance { h =>
    for {
      caption <- h.get[Option[String]]("caption")
      isTitle  <- h.get[Option[Boolean]]("isTitle")
      captionAlign<- h.get[Option[String]]("captionAlign")
      factorId <- h.get[Option[Int]]("factorId")
      eventId <- h.get[Option[Long]]("eventId")
      value <- h.get[Option[Double]]("value")
      cartEventName <- h.get[Option[String]]("cartEventName")
      cartEventNameParametered <- h.get[Option[String]]("cartEventNameParametered")
      cartQuoteName <- h.get[Option[String]]("cartQuoteName")
      cartQuoteNameParametered <- h.get[Option[String]]("cartQuoteNameParametered")
    } yield Cell(caption,isTitle,captionAlign,factorId, eventId , value, cartEventName,
      cartEventNameParametered , cartQuoteName, cartQuoteNameParametered)
  }

  implicit val decoderRow: Decoder[Row] = Decoder.instance { h =>
    for {
      isTitle  <- h.get[Option[Boolean]]("isTitle")
      cells <- h.get[List[Cell]]("cells")
    } yield Row(isTitle,cells)
  }

  implicit val decoderMarket: Decoder[Market] = Decoder.instance { h =>
    for {
      marketId <- h.get[Int]("marketId")
      ident <- h.get[String]("ident")
      sortOrder <- h.get[Int]("sortOrder")
      caption <- h.get[String]("caption")
      commonHeaders <- h.get[List[Int]]("commonHeaders")
      rows <- h.get[List[Row]]("rows")
    } yield Market(
      marketId ,
      ident ,
      sortOrder,
      caption,
      commonHeaders ,
      rows
    )
  }

  implicit val decoderEvent: Decoder[Event] = Decoder.instance { h =>
    for {
      id <- h.get[Long]("id")
      num <- h.get[Int]("number")
      startTimeTimestamp <- h.get[Long]("startTimeTimestamp")
      competitionId <- h.get[Option[Int]]("competitionId")
      competitionName <- h.get[Option[String]]("competitionName")
      competitionCaption <- h.get[Option[String]]("competitionCaption")
      competitionsGroupId <- h.get[Option[Int]]("competitionsGroupId")
      competitionsGroupCaption <- h.get[Option[String]]("competitionsGroupCaption")
      skId <- h.get[Int]("skId")
      skName <- h.get[String]("skName")
      skSortOrder <- h.get[String]("skSortOrder")
      regionId <- h.get[Option[Int]]("regionId")
      team1Id <- h.get[Int]("team1Id")
      team2Id <- h.get[Int]("team2Id")
      team1 <- h.get[String]("team1")
      team2 <- h.get[String]("team2")
      statisticsType <- h.get[String]("statisticsType")
      eventName <- h.get[String]("eventName")
      name <- h.get[String]("name")
      place <- h.get[String]("place")
      priority <- h.get[Int]("priority")
      kind <- h.get[Int]("kind")
      rootKind <- h.get[Int]("rootKind")
      sortOrder <- h.get[String]("sortOrder")
      sportViewId <- h.get[Int]("sportViewId")
      allFactorsCount <- h.get[Int]("allFactorsCount")
      markets <- h.get[Option[List[Market]]]("markets")
    } yield Event(
      id,
      num ,
      startTimeTimestamp,
      competitionId ,
      competitionName,
      competitionCaption,
      competitionsGroupId ,
      competitionsGroupCaption,
      skId ,
      skName ,
      skSortOrder,
      regionId ,
      team1Id ,
      team2Id,
      team1 ,
      team2,
      statisticsType ,
      eventName ,
      name ,
      place ,
      priority ,
      kind,
      rootKind,
      sortOrder,
      sportViewId ,
      allFactorsCount,
      markets
    )
  }

  implicit val decoderFonbetLine: Decoder[FonbetLine] = Decoder.instance { h =>
    for {
      result <- h.get[String]("result")
      request <- h.get[String]("request")
      place<- h.get[String]("place")
      lang<- h.get[String]("lang")
      events <- h.get[List[Event]]("events")
    } yield FonbetLine(result, request, place, lang, events)
  }

}

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

val blockingPool = Executors.newCachedThreadPool()//.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create
val uri = uri"https://line36a.bkfon-resource.ru/line/topEvents3?place=line&sysId=1&lang=ru&scopeMarket=1600"

val lstHeader: List[Header] = List(
  Header("Accept", "application/json")
  , Header("Accept-Charset", "utf-8")
  , Header("Accept-Encoding", "gzip")
)

val request = Request[IO](Method.GET, uri, HttpVersion.`HTTP/2.0`, Headers(lstHeader))
val gzClient = GZip()(httpClient)
val httpReq :IO[String] = gzClient.expect[String](request)

import FonbetLineDecoders._
import FonbetObject._
import CommonFuncs._

val parseStringToModel: String => Either[io.circe.Error,FonbetLine] = input =>
  parser.decode[FonbetLine](input)

type SkId = Int

import cats.implicits._

val outEventHeader: Event => IO[Unit] = e =>
  IO{ println(s"${e.competitionCaption}   ${e.team1} - ${e.team2} " +
    s"         [${getDateAsString(convertLongToDate(e.startTimeTimestamp*1000))}]") }

val outEvents: (FonbetLine,SkId) => IO[Unit] = (fbl,skId) => for {
  _ <- IO { println("-------------------------------------------------------------------------------")}
  _ <- fbl.events.filter(e => e.skId == skId )
    .sortBy(e => e.startTimeTimestamp)
    .traverse(e =>
      outEventHeader(e) *>

        //основное время
       IO{ println(s"  ${e.markets.get.head.caption} ${e.markets.get.head.rows.get(1).fold("1.None")(r => r.cells.head.caption.getOrElse("---"))}"+
       s" ${e.markets.get.head.rows.get(1).fold("2.None"){
         r => s"           ${r.cells.get(1).head.value.getOrElse(0.0)} - ${r.cells.get(3).head.value.getOrElse(0.0)} "
       }} ")} *>

       //Итоговая победа
        IO{ println(s"  ${e.markets.get.head.caption} ${e.markets.get.head.rows.get(2).fold("1.None")(r => r.cells.head.caption.getOrElse("---"))}"+
          s" ${e.markets.get.head.rows.get(2).fold("2.None"){
            r => s"           ${r.cells.get(1).head.value.getOrElse(0.0)} - ${r.cells.get(3).head.value.getOrElse(0.0)} "
          }} ")} *>

       IO { println("-------------------------------------------------------------------------------")}
    )
} yield ()

val outFootbool : FonbetLine => IO[Unit] = fbl =>
  outEvents(fbl,1)

/*
  skId
competitionCaption
*/
/*rp_pfhd_f2020*/
/*
topEvents3
*/

val app2 :IO[Unit] = for {
  resString <- httpReq
  _ <- parseStringToModel(resString).fold(
    er => IO{ println(er.toString) },
    sc => outFootbool(sc)
  )
} yield ()

app2.unsafeRunSync

/*
import scala.concurrent.duration.{DurationInt, FiniteDuration}
val dur: FiniteDuration = 30.seconds
_  <- IO.sleep(10.seconds)
*/








