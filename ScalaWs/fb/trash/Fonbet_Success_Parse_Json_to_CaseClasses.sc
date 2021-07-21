import cats.effect.{Blocker, ContextShift, IO, Timer}
import java.util.concurrent._

import io.circe.parser
import org.http4s.{Header, Headers, HttpVersion, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._
import io.circe.Decoder

import scala.concurrent.ExecutionContext.global
import org.http4s.client.middleware.GZip

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

object FonbetObject {

  case class Cell(
                   caption: Option[String],
                   isTitle: Option[Boolean]
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


}



object FonbetLineDecoders {
  import FonbetObject._

  implicit val decoderCell: Decoder[Cell] = Decoder.instance { h =>
    for {
      caption <- h.get[Option[String]]("caption")
      isTitle  <- h.get[Option[Boolean]]("isTitle")
    } yield Cell(caption,isTitle)
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

val blockingPool = Executors.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create
val uri = uri"https://line36a.bkfon-resource.ru/line/topEvents3?place=line&sysId=1&lang=ru&scopeMarket=1600"

val lstHeader: List[Header] = List(
  Header("Accept", "application/json")
  , Header("Accept-Charset", "utf-8")
  , Header("Accept-Encoding", "gzip")
)

val gzClient = GZip()(httpClient)


val request: IO[Request[IO]] = IO(Request[IO](Method.GET, uri, HttpVersion.`HTTP/2.0`, Headers(lstHeader)))
val httpReq: Request[IO] => IO[String] = request => gzClient.expect[String](request)

/*
import io.circe.Decoder
import io.circe.generic.JsonCodec
import io.circe.syntax._
*/
import FonbetLineDecoders._
import FonbetObject._

val parseStringToModel: String => Either[io.circe.Error,FonbetLine] = input =>
  parser.decode[FonbetLine](input)

val ioa: Int => IO[Unit] = s => IO { println(s"hey - $s") }

import cats.implicits._
val outputEvents : FonbetLine => IO[Unit] = se =>
  for {
    _ <- se.events.traverse(e => IO{println(e.eventName)})
  } yield ()

val app :IO[Unit] = for {
  _ <- ioa(1)
  req <- request
  httpr <- httpReq(req)
  responseStr <- httpr
  //_ <- ioa(2)
  resEither <- IO(parser.decode[FonbetLine](responseStr))
  //_ <- ioa(3)
  //res <- resEither.fold(
 //   er => IO(er.toString),
  //  sc => outputEvents(sc)
  //)
 // _ <- ioa(4)
} yield ()

app.unsafeRunSync