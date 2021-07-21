import zio.{App, ExitCode, Has, Runtime, UIO, ULayer, ZEnv, ZIO, ZLayer}
import zio.console.putStrLn

object JustContainerObject {

  sealed trait Auth
  final object Success extends Auth{
    override  def toString: String = "Success"
  }
  final object Fail extends Auth{
    override  def toString: String = "Fail"
  }

  case class UserStatus(userId: String, status: Auth, failedBy: Option[String])

  type FacebookAuth = Has[FacebookAuth.Service]

  object FacebookAuth {
    trait Service {
      val name: Option[String] = Some("Facebook")
      def checkUser(userId: String): UIO[UserStatus]
    }
    class ServiceImpl extends Service {
      override def checkUser(userId: String): UIO[UserStatus] =
        if (List("234","567").contains(userId)) {
          UIO(UserStatus(userId, Fail, name))
        } else {
          UIO(UserStatus(userId, Success, None))
        }
    }
    val live: ULayer[FacebookAuth] = ZLayer.succeed(new ServiceImpl)
  }

}

import JustContainerObject._

object MyApp extends App {

  lazy val fbCheckerLayer: ULayer[FacebookAuth] = FacebookAuth.live

  val program: List[String] => ZIO[FacebookAuth, Nothing, List[UserStatus]] = listUsers =>
    for {
      fbChecker <- ZIO.service[FacebookAuth.Service]
      lstUsersChecked <- ZIO.foreach(listUsers)(fbChecker.checkUser)
    } yield lstUsersChecked

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val prg = for {
      users  <- program(args).provideCustomLayer(fbCheckerLayer)
      _ <- ZIO.foreach(users)(u => putStrLn(s"[${u.userId}] - ${u.status} (${u.failedBy})"))
    } yield ()
    prg.exitCode
  }
}

val runtime = Runtime.default
runtime.unsafeRun(MyApp.run(List("123","234","456","567","678")))