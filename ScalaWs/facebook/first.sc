import cats.effect.{Blocker, ContextShift, IO, Timer}
import java.util.concurrent._

import cats.implicits.catsSyntaxApply
import org.http4s.{Header, Headers, HttpVersion, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s._

import scala.concurrent.ExecutionContext.global
import org.http4s.client.middleware.GZip
import io.circe.Decoder

object CommonObject {
  case class AccessToken(
                          access_token: String,
                          token_type: String
                 )
  case class App(
                id: String,
                name: String,
                contact_email: String
                )
  case class User(
                  id: String,
                  name: String,
                  birthday: Option[String],
                  email: Option[String]
                )
  implicit val decoderAccessToken: Decoder[AccessToken] = Decoder.instance { h =>
    for {
      access_token  <- h.get[String]("access_token")
      token_type <- h.get[String]("token_type")
    } yield AccessToken(access_token,token_type)
  }
  implicit val decoderApp: Decoder[App] = Decoder.instance { h =>
    for {
      id  <- h.get[String]("id")
      name <- h.get[String]("name")
      contact_email <- h.get[String]("contact_email")
    } yield App(id,name,contact_email)
  }

  implicit val decoderUser: Decoder[User] = Decoder.instance { h =>
    for {
      id  <- h.get[String]("id")
      name <- h.get[String]("name")
      birthday <- h.get[Option[String]]("birthday")
      email <- h.get[Option[String]]("email")
    } yield User(id,name,birthday,email)
  }


}

implicit val cs: ContextShift[IO] = IO.contextShift(global)
implicit val timer: Timer[IO] = IO.timer(global)

val blockingPool = Executors.newCachedThreadPool()
val blocker = Blocker.liftExecutorService(blockingPool)
val httpClient: Client[IO] = JavaNetClientBuilder[IO](blocker).create

val clienAppId: String = "619081065449150"
val clientSecret: String = "c0994b5c2a76624c654ffd0962100e4c"

/* mfm
val clienAppId: String = "365766817503376"
val clientSecret: String = "64239c3e8c08db610ecda5f62f94f248"
*/

val uriString: String = s"https://graph.facebook.com/oauth/access_token?client_id=$clienAppId&client_secret=$clientSecret&grant_type=client_credentials"
val uriAppToken: Uri = Uri.unsafeFromString(uriString)

val uriAbout: String => Uri = access_token =>
  Uri.unsafeFromString(s"https://graph.facebook.com/365766817503376?fields=id,name,contact_email&access_token=$access_token")

val uriAboutUser: String => Uri = access_token =>
  Uri.unsafeFromString(s"https://graph.facebook.com/10218233071161878?fields=id,name,birthday,email&access_token=$access_token")
//,birthday,email


val lstHeader: List[Header] = List(
  Header("Accept", "application/json")
  , Header("Accept-Charset", "utf-8")
  , Header("Accept-Encoding", "gzip")
)

val requestAppToken: Request[IO] = Request[IO](Method.GET, uriAppToken, HttpVersion.`HTTP/2.0`, Headers(lstHeader))
val requestAbout: String => Request[IO] = access_token => Request[IO](Method.GET, uriAbout(access_token), HttpVersion.`HTTP/2.0`, Headers(lstHeader))
val requestAboutUser: String => Request[IO] = access_token => Request[IO](Method.GET, uriAboutUser(access_token), HttpVersion.`HTTP/2.0`, Headers(lstHeader))



val gzClient = GZip()(httpClient)

val httpReqAppToken :IO[String] = gzClient.expect[String](requestAppToken)
val httpReqAbout : String => IO[String] = access_token => gzClient.expect[String](requestAbout(access_token.replace("|","%7C")))

val httpReqUser : String => IO[String] = access_token => gzClient.expect[String](requestAboutUser(access_token.replace("|","%7C")))


import io.circe.parser
import org.http4s.implicits._

import CommonObject._
val parseAccessType: String => Either[io.circe.Error,AccessToken] = input =>
  parser.decode[AccessToken](input)

val parseApp: String => Either[io.circe.Error,App] = input =>
  parser.decode[App](input)

val parseUser: String => Either[io.circe.Error,User] = input =>
  parser.decode[User](input)

val app1 :IO[Unit] = for {
  resAppToken <- httpReqAppToken
  at <- parseAccessType(resAppToken).fold(fa => IO{println(fa.getMessage)},
    sc =>
    IO{println(s"App token = ${sc.access_token}")} *>
      httpReqAbout(sc.access_token).flatMap(about =>
        parseApp(about).fold(fa => IO{println(fa.getMessage)},
          sc => IO{println(s"Application: ${sc.id} - ${sc.name} - ${sc.contact_email}")})) *>
      httpReqUser(sc.access_token).flatMap(about =>
        parseUser(about).fold(fa => IO{println(fa.getMessage)},
          sc => IO{println(s"User: $sc")}))
  )
  //resString <- httpReqAbout(resAppToken)
  //_ <- IO{println(resString)}
} yield ()

app1.unsafeRunSync






