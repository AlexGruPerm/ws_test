import cats.effect.{Blocker, ContextShift, IO, Timer}
import java.util.concurrent._

import cats.implicits.catsSyntaxApply
import org.http4s.{Header, Headers, HttpVersion, Method, Request}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s._

import scala.concurrent.ExecutionContext.global
import org.http4s.client.middleware.GZip
import io.circe.Decoder
import org.http4s.implicits.http4sLiteralsSyntax


object CommonObject {
  case class AccessToken(
                          access_token: String,
                          user_id: Long
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
      user_id <- h.get[Long]("user_id")
    } yield AccessToken(access_token,user_id)
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

val clienAppId: String = "1718120478361200"
val clientSecret: String = "ecf54f9ce230c159943476d1bafeb92e"
val code: String = "AQDPELb7qZR_MOnqTQXgTT1rPP2GONdsiTrfZ5A1VPWYj2lUwH8A6VEYDg9wGBr91BogyAbrJwtnE5ev0rCh1OE1QqIUGbA-DA8KNoccvs6negGkZUSTloBz4Zy7DrGDJB72X4hptRP-qq3_cjOvb_lbv4lEly_Gy3Y4YKZt7ftVqfp_M8HFRa6WTbgWu7HpBrvMBHkuSDdsEj1F4uKfE8fPCI_wp32UQ7qD3WgKJoHzRw"
//without the #_ portion

val lstHeader: List[Header] = List(
  Header("Accept", "application/json")
  , Header("Accept-Charset", "utf-8")
  , Header("Accept-Encoding", "gzip")
)

case class AuthResponse(access_token: String, user_id: String)

//val requestAppToken: Request[IO] = Request[IO](Method.POST, uriAppToken, HttpVersion.`HTTP/2.0`, Headers(lstHeader))
//implicit val authResponseEntityDecoder: EntityDecoder[IO, AuthResponse] = null

import org.http4s.client.dsl.io._
import org.http4s.Method._

val requestAppToken = POST(
  UrlForm(
    "client_id" -> clienAppId,
    "client_secret" -> clientSecret,
    "grant_type" -> "authorization_code",
    "redirect_uri" -> "https://github.com/AlexGruPerm",
    "code" -> code
  ),
  uri"https://api.instagram.com/oauth/access_token?"
)


val gzClient = GZip()(httpClient)

//val httpReqAppToken :IO[String] = gzClient.expect[String](requestAppToken)

val httpReqAppToken :IO[String] = httpClient.expect[String](requestAppToken)

//val httpReqAbout : String => IO[String] = access_token => gzClient.expect[String](requestAbout(access_token.replace("|","%7C")))
//val httpReqUser : String => IO[String] = access_token => gzClient.expect[String](requestAboutUser(access_token.replace("|","%7C")))


import io.circe.parser
import org.http4s.implicits._

import CommonObject._
val parseAccessToken: String => Either[io.circe.Error,AccessToken] = input =>
  parser.decode[AccessToken](input)

/*
val parseApp: String => Either[io.circe.Error,App] = input =>
  parser.decode[App](input)

val parseUser: String => Either[io.circe.Error,User] = input =>
  parser.decode[User](input)*/

val app1 :IO[Unit] = for {
  resAppToken <- httpReqAppToken

  _ <- parseAccessToken(resAppToken).fold(fa => IO{
    println(fa.getMessage)
  },
    sc =>
      IO{
        println(s"App token = ${sc.access_token}")
        println(s"user_id = ${sc.user_id}")
      }
  )


} yield ()

app1.unsafeRunSync
/*
App token = IGQVJVaW1lYWdWRU5VeVZANdWs2WGp4aWtCNEtRcnBvWWFPdmdjWXdUZAGtNbWxCbE9LZAGtiYzQ5SVNoWWFIT0RhUzhMbkh6THNNM0FaVDkxdGU3MXo3RnV5U0N4QURkSENBSkwtXzlHMTBzUHMzTlhKa0V0QXFUZAHhUTVhR
user_id = 17841441851932637
*/


/*
e.FEED_QUERY_ID="ed7dc3bf16156fcfdb12253b4ae43b43"
https://www.instagram.com/graphql/query/?query_hash=ed7dc3bf16156fcfdb12253b4ae43b43&variables=%7B%22has_threaded_comments%22%3Atrue%7D
*/




