import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Date

import cats.effect.{Blocker, ContextShift}
import java.util.concurrent._

import org.http4s.{Header, Headers, HttpVersion, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s._
import cats.effect.{IO, Timer}

import scala.concurrent.duration._
import io.circe.Json
import org.http4s.circe.jsonDecoder

import scala.concurrent.ExecutionContext.global
import org.http4s.implicits.http4sLiteralsSyntax

import scala.reflect.io.File

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
val userAgent: String =
  "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36"
val pageUser: Long = 40007349943L
//1356060226L svetayakusheva
//40007349943L СПЖ
//1529122111L //arena
//1203651107L //alexfitness 59

val rnd = new scala.util.Random
def sleepMs: FiniteDuration = (50 + rnd.nextInt((500 - 50) + 1)).millisecond
def checkLongSleep: Boolean = if (rnd.nextInt(100 + 1) > 70) true else false
def longSleepSec : FiniteDuration = (10 + rnd.nextInt((10 - 2) + 1)).second
def extraLongSleepSec : FiniteDuration = 20.second
def sleepSec : FiniteDuration = (2 + rnd.nextInt((10 - 2) + 1)).second
def getRndDigit: String = (1 + rnd.nextInt((9 - 1) + 1)).toString


object InstgramCommon{

  case class FollowersProps(
                            userid: Long,
                            numFollowers: Int,
                            graphqlAfter: Option[String]
                           )

  case class Follower(
                       id: Option[Long],
                       username: Option[String],
                       full_name: Option[String],
                       is_private: Option[Boolean]
                     )

  case class NextFollowersCall(hasNext: Boolean, after: String, listFollowers: List[Follower])

}
import InstgramCommon._

val HeaderInstagram: List[Header] = List(Header("user-agent", userAgent))


//todo :minimize header, remove unnecessary
//todo: here we need change last digit in x-instagram-ajax
val HeaderLogin: String => List[Header] = csrftoken => List(
  Header("accept", "*/*"),
  Header("accept-encoding", "gzip, deflate, br"),
  Header("accept-language", "en-US,en;q=0.9"),
  Header("cache-control", "no-cache"),
  Header("content-type", "application/x-www-form-urlencoded"),
  Header("origin", "https://www.instagram.com"),
  Header("pragma", "no-cache"),
  Header("referer", "https://www.instagram.com/"),
  Header("sec-fetch-dest", "empty"),
  Header("sec-fetch-mode", "cors"),
  Header("sec-fetch-site", "same-origin"),
  Header("user-agent", userAgent),
  Header("x-csrftoken", csrftoken),
  Header("x-ig-app-id", "936619743392459"),
  Header("x-ig-www-claim", "0"),
  Header("x-instagram-ajax", "894dd53370"+ getRndDigit.toString+ getRndDigit.toString),
  Header("x-requested-with", "XMLHttpRequest")
)

val HeaderFollowers: List[Header] = List(
  Header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"),
  Header("Accept-Language", "en-US,en;q=0.5"),
  Header("cache-control", "no-cache"),
  Header("pragma", "no-cache"),
  Header("sec-fetch-dest", "document"),
  Header("sec-fetch-mode", "navigate"),
  Header("sec-fetch-site", "none"),
  Header("x-ig-www-claim", "hmac.AR0z1Y3ixzlxC2p2MsuQ0aCwFNW3C-wX1niG8RPXcbuL11RU"),
  Header("upgrade-insecure-requests", "1"),
  Header("user-agent", userAgent)
)

val vars: FollowersProps => String = fprop => {
  fprop.graphqlAfter match {
    case Some(after) => "{\"id\":\""+fprop.userid+"\",\"include_reel\":true,\"fetch_mutual\":true,\"first\":" + fprop.numFollowers + ",\"after\":\"" + after + "\"}"
    case None => "{\"id\":\""+fprop.userid+"\",\"include_reel\":true,\"fetch_mutual\":true,\"first\":" + fprop.numFollowers + "}"
  }
}

/**
 * If you don't care about compile time verification of whole URL,
 * then probably this solution is a bit cleaner:
 * val uriFoll: Uri = uri"https://www.instagram.com/graphql/query/"
 * .withQueryParam("query_hash", "c76146de99bb02f6415203be841dd25a")
 * .withQueryParam("variables", """{"id":"374397360","include_reel":false,"fetch_mutual":false,"first":24}"""
*/

val varsEncoded: FollowersProps => String = fprop => Uri.encode(vars(fprop))
val uriFoll: FollowersProps => Uri = fprop =>
  Uri.unsafeFromString(s"https://www.instagram.com/graphql/query/?query_hash=c76146de99bb02f6415203be841dd25a&variables=${varsEncoded(fprop)}")

val reqInstagram: Request[IO] = Request[IO](
  Method.GET,
  uri"https://www.instagram.com/",
  HttpVersion.`HTTP/2.0`,
  Headers(HeaderInstagram)
)

val reqInstagramLogin: (UrlForm, List[Header]) => Request[IO] = (formData,lstHeader) => Request[IO](
  Method.POST,
  uri"https://www.instagram.com/accounts/login/ajax/",
  HttpVersion.`HTTP/2.0`,
  Headers(lstHeader)
).withEntity(formData)

val reqInstFollowers: (FollowersProps,List[Header]) => Request[IO] = (fprop, lstHeader) => {
  //println(s" >>>>>>>>>>>>>>> AFTER = ${fprop.graphqlAfter}")
  Request[IO](
    Method.GET,
    uriFoll(fprop),
    HttpVersion.`HTTP/2.0`,
    Headers(lstHeader)
  )
}

val addCookiesToRequest: (List[ResponseCookie], Request[IO]) => Request[IO] = (lstRespCookies, req) =>
    lstRespCookies.foldLeft(req) { case (req, cookie) =>
      req.addCookie(cookie.name, cookie.content)
    }

val httpReqInstagram: IO[Response[IO]] = httpClient.run(reqInstagram).use(resp => IO{resp})

val httpReqLogin: Request[IO] => IO[Response[IO]] = req => httpClient.run(req).use(resp => IO{resp})

val httpReqFoll : Request[IO] => IO[Json] = req => {
  /*
  println("----------------------------------------------------------------------------------------------------")
  println("QUERY: " + req.uri.renderString)
  println("----------------")
  req.headers.foreach(h => println(s"HEADER: ${h.name} : ${h.value}"))
  println("----------------")
  req.cookies.foreach(rc => println(s"COOKIE: ${rc.name} : ${rc.content}"))
  println("----------------------------------------------------------------------------------------------------")
  */
  httpClient.expect[Json](req)
}

val csrftoken: Response[IO] => Option[String] = resp =>
  resp.cookies.find(_.name == "csrftoken").map(_.content)

val printHeadersDebug: Headers => IO[Unit] = headers => for {
  _ <- IO{ println("============HEADERS=================") }
  _ <- IO {headers.foreach {
      h => println(s" ${h.name}  [${h.value}]")
    }
  }
} yield ()

val frmData: UrlForm = UrlForm(
  "username" -> "alexgruperm",
  "enc_password" -> "#PWD_INSTAGRAM_BROWSER:0:0:qwertyu123456",
  "queryParams" -> "{}",
  "optIntoOneTap" -> "false"
)

val printCookiesResp: List[ResponseCookie] => IO[Unit] = lstCookies => for {
  _ <- IO {
    println("= Response Cookies ========================================")
    lstCookies.foreach{
      c => println(s"${c.name} : ${c.content}")
    }
    println("=========================================")
  }
} yield ()

val printCookiesReq: List[RequestCookie] => IO[Unit] = lstCookies => for {
  _ <- IO {
    println("= Request Cookies ========================================")
    lstCookies.foreach{
      c => println(s"${c.name} : ${c.content}")
    }
    println("=========================================")
  }
} yield ()

import io.circe.optics.JsonPath._
import io.circe._

object InstaDecoders{

   val decoderFollower: Json => Follower = json => {
     val node: ACursor = json.hcursor.downField("node")
     Follower(
       node.get[Long]("id").toOption,
       node.get[String]("username").toOption,
       node.get[String]("full_name").toOption,
       node.get[Boolean]("is_private").toOption
     )
   }

}
import InstaDecoders._

val _hasNextPage = root.data.user.edge_followed_by.page_info.has_next_page.boolean
val _endCursor = root.data.user.edge_followed_by.page_info.end_cursor.string

val hasNextPage: Json => Option[Boolean] = json => _hasNextPage.getOption(json)
val endCursor: Json => Option[String] = json => _endCursor.getOption(json)

val getFollowers: Json => Vector[Json] = json =>
  json.hcursor.
    downField("data").
    downField("user").
    downField("edge_followed_by").
    downField("edges").
    focus.
    flatMap(_.asArray).
    getOrElse(Vector.empty)



//def pageCntScroll: Int = (5 + rnd.nextInt((25 - 5) + 1))
def pageCntScroll: Int = (10 + rnd.nextInt((25 - 10) + 1))

val loginProcess: IO[List[ResponseCookie]] = for {
  respInstagram <- httpReqInstagram
  csrfMainPage = csrftoken(respInstagram)
  _ <- IO {
    println(s" Main page - Status = ${respInstagram.status} csrf = $csrfMainPage ")
  }
  cookiesInst: List[ResponseCookie] = respInstagram.cookies
  _ <- IO.sleep(sleepSec)
  reqLoginHeaders = HeaderLogin(csrfMainPage.getOrElse("0"))
  reqInstLoginWithHeaders = reqInstagramLogin(frmData, reqLoginHeaders)
  respLogin <- httpReqLogin(addCookiesToRequest(cookiesInst, reqInstLoginWithHeaders))
  csrfLogin = csrftoken(respLogin)
  _ <- IO {
    println(s" Login - Status = ${respLogin.status} csrf = $csrfLogin ")
  }

  //_ <- printHeadersDebug(respLogin.headers)

  cookiesLogin: List[ResponseCookie] = respLogin.cookies
  _ <- IO.sleep(sleepSec)
} yield cookiesLogin


val getNextListFollowers: (List[ResponseCookie], Option[String]) => IO[NextFollowersCall] =
  (cookiesLogin, after) => for {
    _ <- if(checkLongSleep) IO.sleep(longSleepSec) else IO.sleep(sleepMs)
    _ <- if(checkLongSleep) IO.sleep(extraLongSleepSec) else IO.unit
    reqInstFallWithHeadersNext <- IO{reqInstFollowers(FollowersProps(pageUser, pageCntScroll, after), HeaderFollowers)}
    reqInsFollowsWithHdrCookNext: Request[IO] = addCookiesToRequest(cookiesLogin, reqInstFallWithHeadersNext)
    respFollNext <- httpReqFoll(reqInsFollowsWithHdrCookNext)
    hNextPageNext = hasNextPage(respFollNext)
    eCursorNext = endCursor(respFollNext)
    fFollowersNext = getFollowers(respFollNext)
    listFollowersNext = fFollowersNext.map(decoderFollower).toList
    _ <- IO{ println(s"followers_count = ${fFollowersNext.size}   executed for : $after ")}
  } yield NextFollowersCall(hNextPageNext.getOrElse(false),eCursorNext.getOrElse(""),listFollowersNext)

//todo NEED recursive function here

def getFollowersCall(acc: List[Follower], cookiesLogin: List[ResponseCookie], hasNextPage: Boolean, eCursor: Option[String])
:IO[List[Follower]] = for {
  res <- {
    hasNextPage match {
      case true => for {
        nextFollowersCall <- getNextListFollowers(cookiesLogin,eCursor)
        listFollowersNext = nextFollowersCall.listFollowers
        l <- getFollowersCall(acc,cookiesLogin, nextFollowersCall.hasNext, Option(nextFollowersCall.after))
        r <- IO(listFollowersNext ++ l)
        //_ <- IO{println(s"   ACC.SIZE  =  ${r.size}")}
      } yield r
      case false => IO(acc)
    }
  }
} yield res

import CommonFuncs._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.generic.auto._, io.circe.syntax._
import io.circe.{ Decoder, Encoder, Printer }
import java.io.{ File, PrintWriter }

def save_to_file(fname: String, json: Json) ={
  val file = new File(s"$fname.json")
  val writer = new PrintWriter(file)
  writer.print(json)
  writer.close()
}

//todo: fix receiving x-ig-app-id from headers.
val app1 :IO[Unit] = for {
  beginTs <- IO(System.currentTimeMillis())
  _ <- IO{println(s"[${getDateAsString(convertLongToDate(beginTs))}]")}
  cookiesLogin <- loginProcess

  reqInstFallWithHeaders = reqInstFollowers(FollowersProps(pageUser,15,None),HeaderFollowers)
  reqInsFollowsWithHdrCook: Request[IO] = addCookiesToRequest(cookiesLogin,
    reqInstFallWithHeaders)


  respFoll <- httpReqFoll(reqInsFollowsWithHdrCook)
  hNextPage = hasNextPage(respFoll)
  eCursor = endCursor(respFoll)
  fFollowers = getFollowers(respFoll)
  _ <- IO{ println(s"Nas next page = $hNextPage ")}
  _ <- IO{ println(s"end_cursor = $eCursor")}
  _ <- IO{ println(s"followers_count = ${fFollowers.size}")}
  _ <- IO{ println(s" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~") }
  firstListFollowers = fFollowers.map(decoderFollower).toList

  accumulatedList <- getFollowersCall(List.empty[Follower],cookiesLogin,hNextPage.getOrElse(false),eCursor)
  lst = firstListFollowers ++ accumulatedList

  _ <- IO {
    println(lst.zipWithIndex.asJson)
  }

  _ <- IO {save_to_file("C:\\ws_test\\ScalaWs\\INSTAGRAM\\"+pageUser.toString,
    lst.asJson)}

  _ <- IO{ println(s" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~") }
  endTs <- IO(System.currentTimeMillis())
  _ <- IO{println(s"[${getDateAsString(convertLongToDate(endTs))}]")}
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

