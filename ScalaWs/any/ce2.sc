import cats.effect.IO

def putStrLn(line: String): IO[Unit] =
  IO { println(line) }

def f(p1: IO[Unit], p2: IO[Unit]) :IO[Unit] = for {
  _ <- p1
  _ <- p2
} yield ()
/*
p1.flatMap((_: Unit) =>
    p2.map((_: Unit) => ())
  )

*/

f(putStrLn("hi!"), putStrLn("hi!"))

// is equivalent to

val x = putStrLn("hi!")
f(x, x)

f(x, x).unsafeRunSync()

f(putStrLn("hi!"), putStrLn("hi!")).unsafeRunSync()

