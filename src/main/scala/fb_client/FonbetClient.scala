package fb_client

import fb.types.Types.{FonbetClient}
import zio.{Has, UIO, ZLayer}

  object FonbetClientModule {

    trait Service {
      def randomValue: UIO[String]
    }

    case class FonbetClientImpl(p: String) extends FonbetClientModule.Service {
      def randomValue: UIO[String] = UIO(p.toString)
    }

    val live: ZLayer[String, Nothing, FonbetClient] =
      ZLayer.fromFunction(param => FonbetClientImpl(param))

  }

