import cats.effect.{ContextShift, IO, Timer}
import org.http4s.client.{Client, JavaNetClientBuilder}
import cats.effect.Blocker
import java.util.concurrent._

import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.concurrent.ExecutionContext.global
import org.http4s.{ParseResult, Uri}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

val blockingPool = Executors.newFixedThreadPool(5)
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

val browser = JsoupBrowser()

val htmlCont: IO[ParseResult[Uri]] = IO {
  Uri.fromString("https://lenta.ru")
}

def contAsString(u: Uri):IO[String] = httpClient.expect[String](u)

import cats.implicits._
import cats.syntax.traverse._
import cats.instances.list._

val program: IO[Unit] =
  for {
    pr <- htmlCont
    cs <- pr.fold(f => IO{""},s => contAsString(s))
    lst <- IO{browser.parseString(cs) >> elementList("a")}
  } yield lst.foreach(e => println(e))

/*
val program1: IO[Unit] =
  for {
    pr <- htmlCont
    cs <- pr.fold(f => IO{""},s => contAsString(s))
    lst <- IO{browser.parseString(cs) >> elementList("a")}
    _ <- lst.traverse(e => IO{println(e)})
  } yield ()
*/


program.unsafeRunSync