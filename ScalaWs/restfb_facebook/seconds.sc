import scala.concurrent.duration._

val rnd = new scala.util.Random

//(1 to 20).foreach(_ => println(    rnd.nextInt(100 + 1)     ))

(1 to 30).foreach(_ => println(
  1 + rnd.nextInt((9 - 1) + 1)
))