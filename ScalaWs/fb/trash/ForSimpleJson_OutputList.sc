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

object ItemObject {
  case class Item(userId: Int,
                        id: Int,
                        title: String,
                        completed: Boolean)
}

object ItemDecoders {
  import ItemObject._
  implicit val decoderItem: Decoder[Item] = Decoder.instance { h =>
    for {
      userId <- h.get[Int]("userId")
      id <- h.get[Int]("id")
      title<- h.get[String]("title")
      completed<- h.get[Boolean]("completed")
    } yield Item(userId, id, title, completed)
  }
}

val blockingPool = Executors.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create
val uri = uri"https://jsonplaceholder.typicode.com/todos/1"

val lstHeader: List[Header] = List(
  Header("Accept", "application/json")
  , Header("Accept-Charset", "utf-8")
  , Header("Accept-Encoding", "gzip")
)

val request = Request[IO](Method.GET, uri, HttpVersion.`HTTP/2.0`, Headers(lstHeader))
val gzClient = GZip()(httpClient)
val httpReq :IO[String] = gzClient.expect[String](request)

import ItemDecoders._
import ItemObject._

val parseStringToModel: String => Either[io.circe.Error,Item] = input =>
  parser.decode[Item](input)

val app1 :IO[String] = for {
  resString <- httpReq
  res <- parseStringToModel(resString).fold(
    er => IO(er.toString),
    sc => IO(s"${sc.title}")
  )
} yield res


val outputEvents : Item => IO[String] = se => for {
  _ <- IO{println(se.id)}
  _ <- IO{println(se.userId)}
  _ <- IO{println(se.title)}
 r <- IO {se.title}
} yield r

val app2 :IO[String] = for {
  resString <- httpReq
  res <- parseStringToModel(resString).fold(
    er => IO(er.toString),
    sc => outputEvents(sc)
  )
} yield res

app2.unsafeRunSync









