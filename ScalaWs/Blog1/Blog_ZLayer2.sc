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
      val checkUser : String => UIO[UserStatus]
    }
    class ServiceImpl extends Service {
      override val checkUser : String => UIO[UserStatus] = userId =>
        if (List("234","567").contains(userId)) {
          UIO(UserStatus(userId, Fail, name))
        } else {
          UIO(UserStatus(userId, Success, None))
        }
    }
    val live: ULayer[FacebookAuth] = ZLayer.succeed(new ServiceImpl)
  }

  type GoogleAuth = Has[GoogleAuth.Service]

  object GoogleAuth {
    trait Service {
      val name: Option[String] = Some("Google")
      val checkUser : String => UIO[UserStatus]
    }
    class ServiceImpl extends Service {
      override val checkUser : String => UIO[UserStatus] = userId =>
        if (List("456").contains(userId)) {
          UIO(UserStatus(userId, Fail, name))
        } else {
          UIO(UserStatus(userId, Success, None))
        }
    }
    val live: ULayer[GoogleAuth] = ZLayer.succeed(new ServiceImpl)
  }

  type AllAuth = FacebookAuth with GoogleAuth

}

import JustContainerObject._

object MyApp extends App {

  lazy val fbCheckerLayer: ULayer[FacebookAuth] = FacebookAuth.live
  lazy val glCheckerLayer: ULayer[GoogleAuth] = GoogleAuth.live

  val program: List[String] => ZIO[AllAuth, Nothing, List[UserStatus]] = listUsers =>
    for {
      fbChecker <- ZIO.service[FacebookAuth.Service]
      glChecker <- ZIO.service[GoogleAuth.Service]
      lstUsersCheckedFb <- ZIO.foreach(listUsers)(fbChecker.checkUser)
      lstSuccFb <- ZIO.filter(lstUsersCheckedFb)(ust => UIO(ust.failedBy.isEmpty))
      lstSuccFbUserId = lstSuccFb.map(ust => ust.userId)
      lstUserIdFailFb <- ZIO.filterNot(lstUsersCheckedFb)(ust => UIO(ust.failedBy.isEmpty))
      lstUsersCheckedGl <- ZIO.foreach(lstSuccFbUserId)(glChecker.checkUser)
      res = (lstUserIdFailFb ++ lstUsersCheckedGl).sortBy(us => us.userId)
    } yield res

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val prg = for {
      users  <- program(args).provideCustomLayer(fbCheckerLayer ++ glCheckerLayer)
      _ <- ZIO.foreach(users)(u => putStrLn(s"[${u.userId}] - ${u.status} (${u.failedBy})"))
    } yield ()
    prg.exitCode
  }
}

val runtime = Runtime.default
runtime.unsafeRun(MyApp.run(List("123","234","456","567","678")))