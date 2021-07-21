import zio.ZIO
import zio.Task
import scala.io.StdIn
import zio._

/**
 * *****************************
*/

def printLine(line: String) =
  ZIO.effect(println(line))

val app2 = printLine("123").repeatN(3)

object OurApplication extends App {
  def run(args: List[String]) =
    app2.exitCode
}

val runtime = Runtime.default

runtime.unsafeRun(OurApplication.run(List()))

