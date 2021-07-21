import org.http4s.client.dsl.io._
import org.http4s.headers._
import org.http4s.MediaType
import org.http4s.Method._

val request = GET(
  uri"https://my-lovely-api.com/",
  Authorization(Credentials.Token(AuthScheme.Bearer, "open sesame")),
  Accept(MediaType.application.json)
)



