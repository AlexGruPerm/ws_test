
object DataObj {
  case class Data(id: Int, v: String)
}

import DataObj._
import cats.effect.IO
val parseStringToModel: String => Either[io.circe.Error,Data] = input =>
  Right(Data(1,input))

val httpReq: IO[String] = IO("abc")

val app1 :IO[String] = for {
  resString <- httpReq
  res <- parseStringToModel(resString).fold(
    er => IO(er.toString),
    sc => IO(s"${sc.v}")
  )
} yield res

val outputEventsName : Data => IO[Unit] = fbl => {
  IO{println(s"${fbl.v}")}
}

val app2 :IO[Unit] = for {
  resString <- httpReq
  res <- parseStringToModel(resString).fold(
    er => IO{println(er.toString)},
    sc => outputEventsName(sc) // IO{println(s"${sc.v}")}
  )
} yield res

app2.unsafeRunSync
