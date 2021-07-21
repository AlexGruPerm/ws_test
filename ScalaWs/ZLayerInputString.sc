import zio.{ZIO, ZLayer}
import fb.types.Types._

val calcZlayer: ZLayer[InputString,Nothing,outputInt] = ZLayer.fromFunction(inputString => inputString.size)

val prg: ZIO[calcZlayer,Nothing,Unit] = for
 p = ZIO.access[calcZlayer](_).map
} yield ()

prg.provideLayer(calcZlayer)

