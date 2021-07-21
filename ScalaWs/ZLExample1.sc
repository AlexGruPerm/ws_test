import zio.{App, ExitCode, Has, Runtime, UIO, ULayer, ZEnv, ZIO, ZLayer}
import zio.console._

object Common {

  val eff: UIO[Int] = ZIO.succeed(10)
  val zl: ULayer[Has[Int]] = eff.toLayer

  /**
   * Mastering_Modularity_in_ZIO_with_Zlayer_Ebook.pdf page 13.
   */
  type ServType = Has[Serv.Service]

  object Serv {

    trait Service {
      def getInt(inpVal: Int): UIO[String]
    }

    val live: ULayer[ServType] = ZLayer.succeed {
      new Service {
        override def getInt(inpVal: Int): UIO[String] =
          ZIO.succeed(inpVal.toString)
      }
    }

  }

}

import Common._

val finalExecution: Int => ZIO[Console with ServType, Nothing, Unit] = v =>
  for {
    res <- ZIO.access[ServType](_.get.getInt(v))
    _   <- putStrLn(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ")
    r   <- res
    _   <- putStrLn(s" value = $r")
    _   <- putStrLn(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ")
  } yield ()

object MyApp extends App {

  lazy val servEnv: ZLayer[Any, Nothing, ServType] = Serv.live

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val prm: Int = args.headOption.getOrElse("0").toInt
    val prg = for {
      _  <- finalExecution(prm).provideCustomLayer(servEnv)
    } yield ()
    prg.exitCode
  }
}

val runtime = Runtime.default
runtime.unsafeRun(MyApp.run(List("123")))