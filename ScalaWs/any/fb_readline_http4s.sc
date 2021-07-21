import cats.effect.{Blocker, ContextShift, IO, Timer}
import java.util.concurrent._


import io.circe.Json
import org.http4s.client.dsl.io._
import org.http4s.{Header, Headers, HttpVersion, MediaType, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.headers._
import org.http4s.Method._
import org.http4s.implicits._
import org.http4s.client.middleware.GZip

import scala.concurrent.ExecutionContext.global
implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.util.zip.{GZIPOutputStream, GZIPInputStream}

import scala.util.Try

val blockingPool = Executors.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create
val uri0 = uri"https://yandex.ru"
val uri1 = uri"https://line36a.bkfon-resource.ru/line/topEvents3?place=line&sysId=1&lang=ru&scopeMarket=1600"
val uri3 = uri"https://jsonplaceholder.typicode.com/todos/1"
val uri4 = uri"https://jsonplaceholder.typicode.com/users"

def decompress(compressed: Array[Byte]): String = {
  val inputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))
  scala.io.Source.fromInputStream(inputStream).mkString
}


val lstHeader: List[Header] =List(
   Header("Accept","text/plain"/*"application/json"*/)
  ,Header("Accept-Charset","utf-8")// utf-8 iso-8859-1
  ,Header("Accept-Encoding","gzip")
)
val reqHeaders: Headers = Headers(lstHeader)

val request2 = Request[IO](Method.GET, uri1, HttpVersion.`HTTP/2.0`, reqHeaders)
/*
import org.http4s.circe._
val httpReq = httpClient.expect[Json](request)
httpReq.unsafeRunSync
*/

import java.nio.charset.StandardCharsets

import com.github.gekomad.scalacompress.Compressors._
import com.github.gekomad.scalacompress.CompressionStats
import com.github.gekomad.scalacompress.DecompressionStats

val httpReq = httpClient.expect[String](request2)
val app = httpReq.map(resString => //resString
  unzipString(resString.getBytes("UTF-8"))
)

  //Gzip.decodeData[String](resString))
/*scala.io.Source.fromBytes(resString.getBytes(), "iso-8859-1").mkString*/
app.unsafeRunSync
//unzipResponse
/*
//Parse raw json
import org.http4s.circe._
request.flatMap(_.as[Json]).unsafeRunSync
*/
/*
//OK as String
val httpReq = httpClient.expect[String](requestFb)
//Response header: Content-Type: application/json; charset=utf-8
import io.circe._, io.circe.parser._
val respJson = httpReq.map(resString => parse("""{"foo": "bar"}"""))
respJson.unsafeRunSync
*/






