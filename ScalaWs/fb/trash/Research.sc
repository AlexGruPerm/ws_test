import cats.effect.{Blocker, ContextShift, IO, Timer}
import java.util.concurrent._
import io.circe.parser
import org.http4s.{Header, Headers, HttpVersion, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits._
import io.circe.Decoder

import scala.concurrent.ExecutionContext.global
import org.http4s.client.middleware.GZip

object TestDataObject {
  case class TestData(userId: Int,
                      id: Int,
                      title: String,
                      completed: Boolean)
}

object TestDataDecoders {
  import TestDataObject._
  implicit val decoderFonbetLine: Decoder[TestData] = Decoder.instance { h =>
    for {
      userId <- h.get[Int]("userId")
      id<- h.get[Int]("id")
      title<- h.get[String]("title")
      completed <- h.get[Boolean]("completed")
    } yield TestData(userId, id, title, completed)
  }
}

val uri = uri"https://jsonplaceholder.typicode.com/todos/1"

val lstHeader: List[Header] = List(
    Header("Accept", "application/json")
  , Header("Accept-Charset", "utf-8")
  , Header("Accept-Encoding", "gzip")
)

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

val blockingPool = Executors.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

val request = Request[IO](Method.GET, uri, HttpVersion.`HTTP/2.0`, Headers(lstHeader))
val gzClient = GZip()(httpClient)
val httpReq :IO[String] = gzClient.expect[String](request)

/*val httpReq2 :IO[String] = IO{
  """
    {
      "userId": 1,
      "id": 1,
      "title": "delectus aut autem",
      "completed": false
    }
   """.stripMargin}*/

import TestDataDecoders._
import TestDataObject._

val parseStringToModel: String => Either[io.circe.Error,TestData] = input =>
  parser.decode[TestData](input)

/*
res0: String = app1 - delectus aut autem
*/
val app1 :IO[String] = for {
  resString <- httpReq
  res <- parseStringToModel(resString).fold(
    er => IO(er.toString),
    sc => IO("app1 - " + sc.title.toString)
  )
} yield res

/*
app2 - delectus aut autem
*/
val app2 :IO[Unit] = for {
  resString <- httpReq
  r <- parseStringToModel(resString).fold(
    er => IO{ println(er.toString) },
    sc => IO{ println("app2 - " + sc.title) }
  )
} yield r

app1.unsafeRunSync


