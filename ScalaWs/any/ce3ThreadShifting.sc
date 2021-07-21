import cats.effect.IO

import scala.concurrent._
implicit val ec = ExecutionContext.global

trait CallBackRes[T]{
  def onError(t :Throwable): Unit
  def onSuccess(v: T): Unit
}

trait Channel {
  def sendBytes(chunk: Int, handler: CallBackRes[Int]): Unit
}

class Test(c: Int){

  def run(c: Channel, chunk: Int): IO[Unit] = {
    IO async { cb =>
      c.sendBytes(chunk, new CallBackRes[Int] {
        def onError(t: Throwable) = cb(Left(t))
        def onSuccess(v: Int) = cb(Right(()))
      })
    }
  }

}

val c: Channel = null

val v = (new Test(3))

v.run(c,3).unsafeRunSync()
