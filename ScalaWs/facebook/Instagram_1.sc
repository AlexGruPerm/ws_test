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
import io.circe.Json
import org.http4s.circe.jsonDecoder

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.global
import org.http4s.implicits.http4sLiteralsSyntax
import scalaz.Applicative.monoidApplicative
import scalaz.Scalaz.ToFunctorOps


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

val lstHeaderLogin: (String, String) => List[Header] = (instAppId, csrftoken) => List(
    Header("content-type","application/x-www-form-urlencoded"),
    Header("user-agent" , userAgent),
    Header("x-ig-app-id" , instAppId),
    Header("x-csrftoken" , csrftoken)
  )

val lstHeaderMy: (String, String) => List[Header] = (instAppId, csrftoken) => List(
  Header("content-type","application/x-www-form-urlencoded"),
  Header("user-agent" , userAgent),
  Header("x-ig-app-id" , instAppId),
  Header("x-csrftoken" , csrftoken)
)

/*
val formData :UrlForm = UrlForm(
  "username" -> "alexgruperm",
  "enc_password" -> "#PWD_INSTAGRAM_BROWSER:0:0:060144Alex#",
  "queryParams" -> "{}",
  "optIntoOneTap" -> "false"
)
*/


val vars: String = "{\"id\":\"1356060226\",\"include_reel\":true,\"fetch_mutual\":true,\"first\":24}"
val varsEncoded: String = Uri.encode(vars)
val uriFoll: Uri = Uri.unsafeFromString(s"https://www.instagram.com/graphql/query/?query_hash=c76146de99bb02f6415203be841dd25a&variables=$varsEncoded")

val reqInstagramFoll: List[Header] => Request[IO] = lstHeader => Request[IO](
  Method.GET,
  uriFoll,
  HttpVersion.`HTTP/2.0`,
  Headers(lstHeader)
)

val reqInstagram: Request[IO] = Request[IO](
  Method.GET,
  uri"https://www.instagram.com/",
  HttpVersion.`HTTP/2.0`,
  Headers(lstHeaderInstagram)
)

val reqInstagramLogin: (UrlForm, List[Header]) => Request[IO] = (formData,lstHeader) => Request[IO](
  Method.POST,
  uri"https://www.instagram.com/accounts/login/ajax/",
  HttpVersion.`HTTP/2.0`,
  Headers(lstHeader)
).withEntity(formData)

val reqInstagramMy: List[Header] => Request[IO] = lstHeader => Request[IO](
  Method.GET,
  uri"https://www.instagram.com/alexgruperm/?__a=1",
  HttpVersion.`HTTP/2.0`,
  Headers(lstHeader)
)

val reqInstPersonPage: List[Header] => Request[IO] = lstHeader => Request[IO](
  Method.GET,
  uri"https://www.instagram.com/yakushevasveta89/?__a=1",
  HttpVersion.`HTTP/2.0`,
  Headers(lstHeader)
)


val addCookiesToRequest: (List[ResponseCookie],Request[IO]) => Request[IO] = (lstCookies, req) => {
  lstCookies.foreach(c => req.addCookie(c.name, c.content))
  req
}

val httpReqInstagram: IO[Response[IO]] = httpClient.fetch(reqInstagram)(resp => IO{resp})


// todo: make common function Cover
val httpReqLogin: Request[IO] => IO[Response[IO]] = req =>
  httpClient.fetch(req)(resp => IO{resp})

/*
val httpReqMy: Request[IO] => IO[Response[IO]] = req =>
  httpClient.fetch(req)(resp => IO{resp})
*/

def httpReqMy(req: Request[IO]): IO[Response[IO]] =
  httpClient.fetch(req)(resp => IO{resp})

val httpReqPersonPage: Request[IO] => IO[Response[IO]] = req =>
  httpClient.fetch(req)(resp => IO{resp})

/*
val httpReqFoll: Request[IO] => IO[Response[IO]] = req =>
  httpClient.fetch(req)(resp => IO{resp})
*/

val httpReqFoll : Request[IO] => IO[Json] = req => httpClient.expect[Json](req)


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

val cookiesInstagram: Response[IO] => List[ResponseCookie] = resp => resp.cookies

val printAllHeadersDebug: Response[IO] => IO[Unit] = resp => for {
  _ <- IO{ println("============HEADERS=================") }
  _ <- IO {
    resp.headers.foreach {
      h => println(s" ${h.name}  [${h.value}]")
    }
  }
} yield ()

val printCookiesDebug: List[ResponseCookie] => IO[Unit] = lstCookies => for {
  _ <- IO {
    lstCookies.foreach{
      c => println(s"$c]")
    }
  }
} yield ()

val frmData: UrlForm = UrlForm(
  "username" -> "alexgruperm",
  "enc_password" -> "#PWD_INSTAGRAM_BROWSER:0:0:060144Alex#",
  "queryParams" -> "{}",
  "optIntoOneTap" -> "false"
)

import CommonFuncs._

/**
 * Good example!
 * https://habr.com/ru/post/486714/
*/
val app1 :IO[Unit] = for {
  //First request - Main page
  beginTs <- IO(System.currentTimeMillis())
  _ <- IO{println(s"[${getDateAsString(convertLongToDate(beginTs))}]")}
  respInstagram <- httpReqInstagram
  _ <- IO{ println("=============BEGIN==================") }
  _ <- IO{ println(s" Status = ${respInstagram.status}") }
  _ <- IO{ println("============CSRF 1====================") }
  csrf1 = csrftoken(respInstagram.headers)
  _ <- IO{ println(s"      $csrf1")}
  _ <- IO{ println("============COOKIES=================") }
  cookiesInst: List[ResponseCookie] = cookiesInstagram(respInstagram)
  //todo: close for debug _ <- printHeadersDebug(cookiesInst)
  //todo: close for debug _ <- printAllHeadersDebug(respInstagram)
  _ <- IO{ println("=============BODY===================") }
  //_ <- IO{ println(respInstagram.bodyAsText) } //todo: bodyAsText into String
  _ <- IO{ println("=============END====================") }
  endTs <- IO(System.currentTimeMillis())
  _ <- IO{println(s"[${getDateAsString(convertLongToDate(endTs))}]")}
  _ <- IO.sleep(2.second)


  //Second request = Login ajax
  _ <- IO{ println("=============BEGIN LOGIN============") }
  //todo: fix receiving x-ig-app-id from headers.
  reqLoginHeaders = lstHeaderLogin("936619743392459",csrf1.getOrElse("0"))
  reqInstLoginWithHeaders = reqInstagramLogin(frmData,reqLoginHeaders)
  respLogin <- httpReqLogin(addCookiesToRequest(cookiesInst,reqInstLoginWithHeaders))
  _ <- IO{ println(s" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~") }
  _ <- IO{ println(s" Status = ${respLogin.status}") }

  _ <- IO{ println("============CSRF 2====================") }
  csrf2 = csrftoken(respLogin.headers)
  _ <- IO{ println(s"      $csrf2")}

  _ <- IO{ println("============COOKIES=================") }
  cookiesLogin: List[ResponseCookie] = cookiesInstagram(respLogin)
  //todo: close for debug
  _ <- printCookiesDebug(cookiesLogin)
  //todo: close for debug
  _ <- printAllHeadersDebug(respLogin)
  _ <- IO{ println("====== EMPTY BODY===================") }
  endTs <- IO(System.currentTimeMillis())
  _ <- IO{println(s"[${getDateAsString(convertLongToDate(endTs))}]")}
  _ <- IO{ println("=============END====================") }


  //Third request = Go to my page
  reqMyHeaders = lstHeaderLogin("936619743392459",csrf2.getOrElse("0"))
  reqInstMyWithHeaders = reqInstagramMy(reqMyHeaders)
  respMy <- httpReqMy(addCookiesToRequest(cookiesLogin,reqInstMyWithHeaders))
  _ <- IO{ println(s" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~") }
  _ <- IO{ println(s" Status = ${respMy.status}") }

  _ <- IO{ println("============CSRF 3====================") }
  csrf3 = csrftoken(respMy.headers)
  _ <- IO{ println(s"      $csrf3")}

  _ <- IO{ println("============COOKIES=================") }
  cookiesMy: List[ResponseCookie] = cookiesInstagram(respMy)
  _ <- printCookiesDebug(cookiesMy)


  //Fourth query - person page
  reqPersonPageHeaders = lstHeaderLogin("936619743392459",csrf3.getOrElse("0"))
  reqPersonPageWithHdr = reqInstPersonPage(reqPersonPageHeaders)
  respPersonPage <- httpReqPersonPage(addCookiesToRequest(cookiesMy,reqPersonPageWithHdr))
  _ <- IO{ println(s" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~") }
  _ <- IO{ println(s" Status [Person Page] = ${respPersonPage.status}") }

  _ <- IO{ println("============CSRF 4====================") }
  csrf4 = csrftoken(respPersonPage.headers)
  _ <- IO{ println(s"      $csrf4")}

  cookiesPersonPage: List[ResponseCookie] = cookiesInstagram(respPersonPage)
  _ <- printCookiesDebug(cookiesPersonPage)

  //Fifth query - person followers
  reqFollHeaders = lstHeaderLogin("936619743392459",csrf4.getOrElse("0"))
  reqInstFallWithHeaders = reqInstagramFoll(reqFollHeaders)
  respFoll <- httpReqFoll(addCookiesToRequest(cookiesPersonPage,reqInstFallWithHeaders))
  _ <- IO{ println(s" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~") }
  _ <- IO{ println(respFoll) }
  _ <- IO{ println(s" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~") }


} yield ()

app1.unsafeRunSync
/*
1356060226 - yakushevasveta89
Одинаково для всех аккаунтов или всех, в которые захожу Я.
query_hash=c76146de99bb02f6415203be841dd25a
Сразу со своей страницы могу вызвать
https://www.instagram.com/yakushevasveta89/
https://www.instagram.com/yakushevasveta89/followers/





*/