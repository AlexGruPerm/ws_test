import cats.effect.{ContextShift, IO, Timer}
import org.http4s._
import org.http4s.client.{Client, JavaNetClientBuilder}
import cats.effect.Blocker
import java.util.concurrent._

//example
//https://stackoverflow.com/questions/41885705/circe-encoders-and-decoders-with-http4s

import scala.concurrent.ExecutionContext.global
implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)
import org.http4s.Uri

val blockingPool = Executors.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

val hello: IO[String] = {
  Uri.fromString("https://lenta.ru").fold(
    f => IO.pure(s"string left - $f"),
    s => httpClient.expect[String](s)
  )
}

hello.unsafeRunSync


