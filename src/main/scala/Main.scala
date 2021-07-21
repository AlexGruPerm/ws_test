import fb.types.Types.FonbetClient
import fb_client.FonbetClientModule
import sun.plugin.javascript.navig4.Layer
import zio.console.{Console, putStrLn}
import zio.{App, ExitCode, Has, Layer, ZEnv, ZIO, ZLayer, console}



object Main extends App {

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {

    val fbc: Layer[Nothing, FonbetClient] = Has("") >>> fbClient

    prg.provideCustomLayer(ZEnv.live ++ fbc)
    .foldM(throwable => putStrLn(s"failure") as ExitCode.failure,
      _ => putStrLn(s"Success exit of application.") as ExitCode.success
    )
  }

  val fbClient: ZLayer[String, Nothing, FonbetClient] = FonbetClientModule.live

  private val prg: ZIO[ZEnv with FonbetClient, Throwable, Unit] =
    for {
      _ <- console.putStrLn("prg begin")
      _ <- console.putStrLn("prg end")
    } yield ()

}

