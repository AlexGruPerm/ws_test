package pkg

import pkg.HelloWorld._
import zio._
import zio.console._
import zio.test.Assertion._
import zio.test._
import zio.test.environment._

object HelloWorld {
  def sayHello: URIO[Console, Unit] =
    console.putStrLn("Hello, World!")
}

object HelloWorldSpec extends DefaultRunnableSpec {
  def spec = suite("HelloWorldSpec")(
    testM("sayHello correctly displays output 1") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assert(output)(equalTo(Vector("Hello, World!\n")))
    },
    testM("sayHello correctly displays output 2") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assert(output)(equalTo(Vector("Hello, World 2!\n")))
    },
    testM("sayHello correctly displays output 3") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assert(output)(equalTo(Vector("Hello, World 3!\n")))
    }
  )
}
