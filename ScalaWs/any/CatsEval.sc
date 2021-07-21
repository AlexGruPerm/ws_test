import cats.effect.IO
import cats.Eval
import cats.implicits._

val rng = new scala.util.Random(0L)

val calcSimple: IO[Int] = IO{rng.nextInt(10)}

def prog(f: IO[Int]): IO[Unit] = for {
  _ <- IO{println("begin calc...")}
  r1 <- f
  _ <- IO{println(s" r1 = $r1")}
  r2 <- f
  _ <- IO{println(s" r2 = $r2")}
  r3 <- f
  _ <- IO{println(s" r3 = $r3")}
} yield ()

prog(calcSimple).unsafeRunSync()

val evNow = Eval.now {
  println("Running expensive calculation...")
  1 + 2 * 3
}

val evLater = Eval.later {
  println("(later) Running expensive calculation...")
  1 + 2 * 3
}

evLater.value
evLater.value

val always = Eval.always {
  println("Running expensive calculation...")
  1 + 2 * 3
}







