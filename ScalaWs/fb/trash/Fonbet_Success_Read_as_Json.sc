import cats.effect.{Blocker, ContextShift, IO, Timer}
import java.util.concurrent._
import org.http4s.{Header, Headers, HttpVersion, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._
import io.circe.parser._
import scala.concurrent.ExecutionContext.global
import org.http4s.client.middleware.GZip

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

val blockingPool = Executors.newFixedThreadPool(5)
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
val httpReq = gzClient.expect[String](request)
val app = httpReq.map(resString => parse(resString))

/*
res0: Either[io.circe.ParsingFailure,io.circe.Json] =
Right({...
*/

app.unsafeRunSync







