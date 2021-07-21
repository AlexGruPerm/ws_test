import cats.effect.IO
import io.circe.{Decoder, parser}

case class A(id: Int)

object ADecodes {
  implicit val decoderA: Decoder[A] = Decoder.instance { h =>
    for {
      id <- h.get[Int]("id")
    } yield A(id)
  }
}

val httpReq :IO[String] = IO(""" {"id":12345} """)

import ADecodes._
def parseStringToModel(input: String) :Either[io.circe.Error,A] = {
  parser.decode[A](input)
}

val app :IO[String] = httpReq.map(
  resString => parseStringToModel(resString)
    .fold(
      er => er.toString,
      sc => s"${sc.id}"
    ))

val program :IO[Unit] = for
  {
  r <- app
  _ <- IO{println(r)}
} yield ()

import scala.concurrent.duration._
program.timeout(1500.millis).unsafeRunSync