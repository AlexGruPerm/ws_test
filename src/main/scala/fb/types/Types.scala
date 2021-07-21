package fb.types

import fb_client.FonbetClientModule
import zio.{Has, ZLayer}

object Types{
  type FonbetClient = Has[FonbetClientModule.Service]
}
