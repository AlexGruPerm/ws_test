import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Date

import cats.effect.{Blocker, ContextShift, IO, Resource, Timer}
import java.util.concurrent._

import cats.implicits.catsSyntaxApply
import org.http4s.{Header, Headers, HttpVersion, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s._
import java.util.concurrent.ScheduledExecutorService

import cats.effect.{Clock, IO, Timer}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import cats.effect.Clock

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.global
import org.http4s.implicits.http4sLiteralsSyntax

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

object CommonFuncs {

  def zo = ZoneOffset.ofHours(+5)

  def convertLongToDate(l: Long): Date = new Date(l)

  //http://tutorials.jenkov.com/java-internationalization/simpledateformat.html
  // Pattern Syntax
  val DATE_FORMAT = "dd.MM.yyyy HH:mm:ss.SSS"

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

val blockingPool = Executors.newCachedThreadPool()
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create
val userAgent: String = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36"

val lstHeaderInstagram: List[Header] = List(Header("user-agent", userAgent))

val lstHeaderLogin: List[Header] = List(
    Header("content-type","application/x-www-form-urlencoded"),
    Header("user-agent" , userAgent),
    Header("x-ig-app-id" , "936619743392459"),
    Header("x-ig-www-claim" , "0"),
    Header("x-instagram-ajax" , "0034bf82aae2"),
    Header("x-requested-with" , "XMLHttpRequest")
  )

val formData :UrlForm = UrlForm(
  "username" -> "alexgruperm",
  "enc_password" -> "#PWD_INSTAGRAM_BROWSER:0:0:060144Alex#",
  "queryParams" -> "{}",
  "optIntoOneTap" -> "false"
)

//val cookie: RequestCookie = RequestCookie()

val reqInstagram: Request[IO] = Request[IO](
  Method.GET,
  uri"https://www.instagram.com/",
  HttpVersion.`HTTP/2.0`,
  Headers(lstHeaderInstagram)
)


/*val req: Request[IO] = Request[IO](
  Method.POST,
  uri"https://www.instagram.com/accounts/login/ajax/",
  HttpVersion.`HTTP/2.0`,
  Headers(lstHeader)
).withEntity(formData)
//.addCookie(cookie)*/

//val httpReqInstagram :IO[String] = httpClient.expect[String](reqInstagram)
//val httpReq :IO[String] = httpClient.expect[String](req)

//val io: Resource[IO,Response[IO]] = httpClient.run(reqCookie)

val httpReqInstagram: IO[Response[IO]] = httpClient.fetch(reqInstagram)(resp => IO{resp})

/**
 * Take from cookies only one, that contains "csrftoken" and
 * extract value between = and ;
*/
val csrftoken: Headers => Option[String] = headers => {
  headers.find(h =>
    h.name.toString.toLowerCase == "Set-Cookie".toLowerCase &&
    h.value.toLowerCase.contains("csrftoken") &&
    h.value.toLowerCase.indexOf("csrftoken") == 0)
    .map(fh => fh.value.substring(fh.value.indexOf("=")+1,fh.value.indexOf(";")))
}

val cookiesInstagram: Headers => Headers = headers => {
  headers.filter(h =>
    h.name.toString.toLowerCase == "Set-Cookie".toLowerCase)
}

val cookiesInstagram2: Response[IO] => List[ResponseCookie] = resp => resp.cookies

val printAllHeadersDebug: Response[IO] => IO[Unit] = resp => for {
  _ <- IO {
    resp.headers.foreach {
      h => println(s" ${h.name}  [${h.value}]")
    }
  }
} yield ()

val printHeadersDebug: Headers => IO[Unit] = hdrs => for {
  _ <- IO {
    hdrs.foreach {
      h => println(s" ${h.name}  [${h.value}]")
    }
  }
} yield ()

val printHeadersDebug2: List[ResponseCookie] => IO[Unit] = lstCookies => for {
  _ <- IO {
    lstCookies.foreach{
      c => println(s"$c]")
    }
  }
} yield ()

import CommonFuncs._
/**
 * Good example!
 * https://habr.com/ru/post/486714/
*/
val app1 :IO[Unit] = for {
  beginTs <- IO(System.currentTimeMillis())
  _ <- IO{println(s"[${getDateAsString(convertLongToDate(beginTs))}]")}
  respInstagram <- httpReqInstagram
  _ <- IO{ println("=============BEGIN==================") }
  _ <- IO{ println(s" Status = ${respInstagram.status}") }
  _ <- IO{ println("============CSRF====================") }
  csrf = csrftoken(respInstagram.headers)
  _ <- IO{ println(s"      $csrf")}
  _ <- IO{ println("============COOKIES=================") }
  //cookiesInst = cookiesInstagram(respInstagram.headers)
  cookiesInst2 = cookiesInstagram2(respInstagram)
  //_ <- printHeadersDebug(cookiesInst)
  _ <- printHeadersDebug2(cookiesInst2)
  _ <- IO{ println("============HEADERS=================") }
  _ <- printAllHeadersDebug(respInstagram)

/*  _ <- IO{ respInstagram.headers.foreach{
    h => println(s" ${h.name}  [${h.value}]")
  }}*/

  _ <- IO{ println("=============BODY===================") }
  _ <- IO{ println(respInstagram.bodyAsText) } //todo: bodyAsText into String
  _ <- IO{ println("=============END====================") }
  endTs <- IO(System.currentTimeMillis())
  _ <- IO{println(s"[${getDateAsString(convertLongToDate(endTs))}]")}
  /*
  res <- httpReqCookie
  _ <- IO{println(res)}
  */
} yield ()

app1.unsafeRunSync

