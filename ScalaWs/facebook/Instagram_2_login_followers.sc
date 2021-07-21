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

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

val blockingPool = Executors.newCachedThreadPool()
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create
val userAgent: String = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36"

val HeaderInstagram: List[Header] = List(Header("user-agent", userAgent))

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
  Header("x-instagram-ajax", "74a6f145b765"),
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
  Header("upgrade-insecure-requests", "1"),
  Header("user-agent", userAgent)
)
/*
 "User-Agent: $useragent",
cookie: ig_did=04E7512D-D2B2-4940-944A-524A50FB6271;
mid=X2pPngAEAAGbN3hvoC5TnWrCiUOH;
csrftoken=OnXpnu0qhf8sFTVZb6HgJhvcTdgOJapa;
ds_user_id=41861099064;
sessionid=41861099064%3A5jbLrGjYNVe5Q0%3A14; rur=RVA;
urlgen="{\"46.146.230.119\": 12768}:1kKnxC:-pumH5yvpU5ur7iGcBoFPZM-lcA"
*/



val vars: String = "{\"id\":\"1356060226\",\"include_reel\":true,\"fetch_mutual\":true,\"first\":24}"
val varsEncoded: String = Uri.encode(vars)
val uriFoll: Uri = Uri.unsafeFromString(s"https://www.instagram.com/graphql/query/?query_hash=c76146de99bb02f6415203be841dd25a&variables=$varsEncoded")

val reqInstagram: Request[IO] = Request[IO](
  Method.GET,
  uri"https://www.instagram.com/",
  HttpVersion.`HTTP/1.0`,
  Headers(HeaderInstagram)
)

val reqInstagramLogin: (UrlForm, List[Header]) => Request[IO] = (formData,lstHeader) => Request[IO](
  Method.POST,
  uri"https://www.instagram.com/accounts/login/ajax/",
  HttpVersion.`HTTP/1.0`,
  Headers(lstHeader)
).withEntity(formData)

val reqInstagramFoll: List[Header] => Request[IO] = lstHeader => Request[IO](
  Method.GET,
  uriFoll,
  HttpVersion.`HTTP/1.0`,
  Headers(lstHeader)
)




/*
val addCookiesToRequest: (List[ResponseCookie],Request[IO]) => Request[IO] = (lstCookies, req) => {
  lstCookies.foldLeft(req) { case (req, cookie) =>
    req.addCookie(cookie.name, cookie.content)
  }
  req
}
*/

val addCookiesToRequest: (List[ResponseCookie], Request[IO]) => Request[IO] = (lstRespCookies, req) =>
    lstRespCookies.foldLeft(req) { case (req, cookie) =>
      req.addCookie(cookie.name, cookie.content)
    }


val httpReqInstagram: IO[Response[IO]] = httpClient.run(reqInstagram).use(resp => IO{resp})

val httpReqLogin: Request[IO] => IO[Response[IO]] = req => httpClient.run(req).use(resp => IO{resp})

val httpReqFoll : Request[IO] => IO[Json] = req => {
  println("----------------------------------------------------------------------------------------------------")
  println("QUERY: " + req.uri.renderString)
  println("----------------")
  req.headers.foreach(h => println(s"HEADER: ${h.name} : ${h.value}"))
  println("----------------")
  //todo: Cookies are absent.
  req.cookies.foreach(rc => println(s"COOKIE: ${rc.name} : ${rc.content}"))
  println("----------------------------------------------------------------------------------------------------")
  httpClient.expect[Json](req)
}

val csrftoken: Response[IO] => Option[String] = resp =>
  resp.cookies.find(_.name == "csrftoken").map(_.content)

val urlgen: Response[IO] => Option[String] = resp =>
  resp.cookies.find(_.name == "urlgen").map(_.content)


val printHeadersDebug: Headers => IO[Unit] = headers => for {
  _ <- IO{ println("============HEADERS=================") }
  _ <- IO {headers.foreach {
      h => println(s" ${h.name}  [${h.value}]")
    }
  }
} yield ()

val frmData: UrlForm = UrlForm(
  "username" -> "alexgruperm",
  "enc_password" -> "#PWD_INSTAGRAM_BROWSER:0:0:060144Alex#",
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

//todo: fix receiving x-ig-app-id from headers.
val app1 :IO[Unit] = for {

  respInstagram <- httpReqInstagram
  csrfMainPage = csrftoken(respInstagram)
  urlgenMainPage = urlgen(respInstagram)
  _ <- IO{ println(s" Status = ${respInstagram.status} $csrfMainPage $urlgenMainPage") }
  cookiesInst: List[ResponseCookie] = respInstagram.cookies
  //_ <- printHeadersDebug(respInstagram.headers)
  _ <- IO.sleep(1.second)

  reqLoginHeaders = HeaderLogin(csrfMainPage.getOrElse("0"))
  reqInstLoginWithHeaders = reqInstagramLogin(frmData,reqLoginHeaders)
  respLogin <- httpReqLogin(addCookiesToRequest(cookiesInst,reqInstLoginWithHeaders))
  csrfLogin = csrftoken(respLogin)
  urlgenLogin = urlgen(respLogin)
  _ <- IO{ println(s" Status = ${respLogin.status} $csrfLogin $urlgenLogin") }
  cookiesLogin: List[ResponseCookie] = respLogin.cookies
  //_ <- printHeadersDebug(respLogin.headers)
  _ <- IO.sleep(1.second)


  /**
  * when we execute www.instagram.com/graphql/query/?query_hash=
   * csrf sending with Cookie , not with header like in HeaderLogin
  */
  reqFollHeaders = HeaderFollowers
  reqInstFallWithHeaders = reqInstagramFoll(reqFollHeaders)


  _ <- printCookiesResp(cookiesLogin)
  reqInsFollowsWithHdrCook: Request[IO] = addCookiesToRequest(cookiesLogin,reqInstFallWithHeaders)
  _ <- printCookiesReq(reqInsFollowsWithHdrCook.cookies)


  respFoll <- httpReqFoll(reqInsFollowsWithHdrCook)
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