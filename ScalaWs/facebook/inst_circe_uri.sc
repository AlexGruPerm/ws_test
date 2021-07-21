import org.http4s._

val vars: String = "{\"id\":\"374397360\",\"include_reel\":false,\"fetch_mutual\":false,\"first\":24}"

val varsEncoded: String = Uri.encode(vars)

val uriFoll: Uri = Uri.unsafeFromString(s"https://www.instagram.com/graphql/query/?query_hash=c76146de99bb02f6415203be841dd25a&variables=$varsEncoded")

