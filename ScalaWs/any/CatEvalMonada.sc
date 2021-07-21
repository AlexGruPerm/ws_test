import cats.Eval
import cats.effect.IO

val a = Eval.always{
  println("Calculating always a");
  10
}

val b = Eval.later{
  println("Calculating later b");
  2
}

val t0 = a.map(v => println(s"output $v"))

val ans = for {
  aRes1 <- a
  bRes1 <- b
  aRes2 <- a
  bRes2 <- b
  aRes3 <- a
  bRes3 <- b
  aRes4 <- a
  bRes4 <- b
} yield {
    aRes1 + bRes1 +
    aRes2 + bRes2 +
    aRes3 + bRes3 +
    aRes4 + bRes4
}

ans.value

//https://books.underscore.io/scala-with-cats/scala-with-cats.html
//4.6 The Eval Monad


