import zio.{App, ExitCode, Has, Runtime, UIO, ULayer, ZEnv, ZIO, ZLayer}
import zio.console.{Console, putStrLn}

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

  type CommAuth = Has[CommAuth.Service]

  object CommAuth {
    trait Service {
      val checkUser : String => ZIO[AllAuth,Nothing,UserStatus]
    }
    class ServiceImpl extends Service {
      override val checkUser : String => ZIO[AllAuth,Nothing,UserStatus] = userId => for {
        fbChecker <- ZIO.service[FacebookAuth.Service]
        glChecker <- ZIO.service[GoogleAuth.Service]
        thisUserFb <- fbChecker.checkUser(userId)
        res <- if(thisUserFb.failedBy.isEmpty)
          glChecker.checkUser(thisUserFb.userId)
        else
          ZIO.succeed(thisUserFb)
      } yield res
    }

  /* v2
    class ServiceImpl extends Service {
      override val checkUser : String => ZIO[AllAuth,Nothing,UserStatus] = userId => for {
        fbChecker <- ZIO.service[FacebookAuth.Service]
        glChecker <- ZIO.service[GoogleAuth.Service]
        thisUserFb <- fbChecker.checkUser(userId)
        res <- if(thisUserFb.failedBy.isEmpty)
          glChecker.checkUser(thisUserFb.userId)
        else
          ZIO.succeed(thisUserFb)
      } yield res
    }*/

/* v1
    class ServiceImpl extends Service {
      override val checkUser : String => ZIO[AllAuth,Nothing,UserStatus] = userId => for {
      fbChecker <- ZIO.service[FacebookAuth.Service]
      glChecker <- ZIO.service[GoogleAuth.Service]
      thisUserFb <- fbChecker.checkUser(userId)
      thisUserGl <- glChecker.checkUser(userId)
      res = if (thisUserFb.failedBy.isEmpty) thisUserGl else thisUserFb
      } yield res
    }*/
    val live: ZLayer[AllAuth,Nothing,CommAuth] = ZLayer.succeed(new ServiceImpl)
  }

}

import JustContainerObject._

object MyApp extends App {

  lazy val CheckerLayer: ZLayer[Any,Nothing,CommAuth] = (FacebookAuth.live ++ GoogleAuth.live) >>> CommAuth.live

  lazy val env = (FacebookAuth.live ++ GoogleAuth.live) ++ CheckerLayer

  val program: List[String] => ZIO[AllAuth with CommAuth, Nothing, List[UserStatus]] = listUsers =>
    for {
      check <- ZIO.service[CommAuth.Service]
      usersChecked <- ZIO.foreach(listUsers)(check.checkUser)
    } yield usersChecked

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val prg = for {
      users  <- program(args).provideCustomLayer(env)
      _ <- ZIO.foreach(users)(u => putStrLn(s"[${u.userId}] - ${u.status} (${u.failedBy})"))
    } yield ()
    prg.exitCode
  }
}

val runtime = Runtime.default
runtime.unsafeRun(MyApp.run(List("123","234","456","567","678")))

