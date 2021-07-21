import zio.console.Console
import zio.{App, ExitCode, Runtime, URIO, ZIO}
import zio.console._

val calc : ZIO[Int,Throwable,Double] =  ZIO.accessM(i => ZIO.effect(10 / i))
  //ZIO.fromFunction(i => 10/i)

val calc2 : URIO[Int,Either[Throwable,Double]] = calc.either

val prg :ZIO[Console,Nothing,Unit] = for {
  param <- ZIO.succeed(0)
  res <- calc2.provide(param)
  _ <- putStrLn(res.fold(_ => "something goes wrong",s => s.toString))
} yield ()

object MyApp extends App {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] = {
    prg.provideLayer(Console.live).as(ExitCode.success)
  }
}

val runtime = Runtime.default
runtime.unsafeRun(MyApp.run(List()))