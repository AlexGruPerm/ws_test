import cats.effect.IO

case class Item(id: Int, v: Int)
case class SetOfItems(s: List[Item])

val ioa1 = IO { println("hey1!") }
val ioa2 = IO { println("hey2!") }

val httpReq: IO[Either[io.circe.Error, SetOfItems]] =
  IO(Right(SetOfItems(
        List(Item(1, 10),
            Item(2, 20),
            Item(3, 30)))
        )
  )

import cats.implicits._
val outputEvents : SetOfItems => IO[Unit] = se =>
  for {
    _ <- se.s.traverse(e => IO{println(e.id)})
  } yield ()

val app :IO[Unit] = for {
  _ <- ioa1
  data <- httpReq
  res <- data.fold(
    er => IO(er.toString),
    sc => outputEvents(sc)
  )
  res2 <- ioa2
} yield res2

app.unsafeRunSync