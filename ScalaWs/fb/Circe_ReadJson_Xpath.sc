
/*
import scala.concurrent.duration.{DurationInt, FiniteDuration}
val dur: FiniteDuration = 30.seconds
_  <- IO.sleep(10.seconds)
*/

import java.time.{Instant, ZoneOffset}

import cats.effect.{Blocker, ContextShift, IO, Timer}
import java.util.concurrent._

import io.circe.{Decoder, HCursor, Json, parser}
import org.http4s.{Header, Headers, HttpVersion, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._

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

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

val blockingPool = Executors.newCachedThreadPool()//.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create
val uri = uri"https://line36a.bkfon-resource.ru/line/topEvents3?place=line&sysId=1&lang=ru&scopeMarket=200"

val lstHeader: List[Header] = List(
  Header("Accept", "application/json")
  , Header("Accept-Charset", "utf-8")
  , Header("Accept-Encoding", "gzip")
)

val request = Request[IO](Method.GET, uri, HttpVersion.`HTTP/2.0`, Headers(lstHeader))
val gzClient = GZip()(httpClient)
val httpReq :IO[String] = gzClient.expect[String](request)

import CommonFuncs._
import cats.implicits._
import io.circe._, io.circe.parser._

val doc: String => IO[Json] = jsonSrc => IO{ parse(jsonSrc).getOrElse(Json.Null) }
val cursor: IO[Json] => IO[HCursor] = js => js.map(jsc => jsc.hcursor)

val events : HCursor => IO[Vector[Json]] = hc =>
  IO{hc.
  downField("events").
  focus.
  flatMap(_.asArray).
  getOrElse(Vector.empty)}

val app :IO[Unit] = for {
  resString <- httpReq
  js <- doc(resString)
  cur <- cursor(IO(js))
  events <- events(cur)
  _ <-  events.filter(e => e.hcursor.downField("skId").as[Int] == Right(1)).traverse {
    e =>
      IO {
        println {
          e.hcursor.downField("skName").as[String] + "  " +
            e.hcursor.downField("competitionName").as[String]
        }} *>
         IO{ println {
            s"    ${e.hcursor.downField("eventName").as[String]}  " +
              s"${getDateAsString(
                convertLongToDate(e.hcursor.downField("startTimeTimestamp").as[Long].getOrElse(0L)*1000)
              )}]"
          }
      } *>
        IO {
          println {"       "+
            e.hcursor.downField("markets").focus.flatMap(_.asArray).
             getOrElse(Vector.empty).get(0).map{mrk =>
              mrk.hcursor.downField("caption").as[String]
            }
          }
        } *>
        IO {
          println {"       "+
            e.hcursor.downField("markets").focus.flatMap(_.asArray).
              getOrElse(Vector.empty).get(0).map{mrk =>
              mrk.hcursor.downField("rows").focus.flatMap(_.asArray).
                getOrElse(Vector.empty).get(1).map{r =>
                r.hcursor.downField("cells").focus.flatMap(_.asArray).
                  getOrElse(Vector.empty).get(1).map{cl =>
                    cl.hcursor.downField("cartQuoteName").as[String] +" "+
                    cl.hcursor.downField("value").as[Double].toString
                }+" - "+
                  r.hcursor.downField("cells").focus.flatMap(_.asArray).
                    getOrElse(Vector.empty).get(3).map{cl =>
                    cl.hcursor.downField("cartQuoteName").as[String] +" "+
                      cl.hcursor.downField("value").as[Double].toString
                  }
              }
            }
          }
        } *>
      IO { println("-------------------------------------------------------------------------------")}
  }
} yield ()

app.unsafeRunSync


