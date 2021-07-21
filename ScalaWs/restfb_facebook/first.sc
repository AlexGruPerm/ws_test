import com.restfb.types.{Place, User}
import com.restfb.{DefaultFacebookClient, Facebook, Parameter, Version}

/*
val accessToken =
  new DefaultFacebookClient(Version.LATEST).obtainExtendedAccessToken(MY_APP_ID,
    MY_APP_SECRET, MY_ACCESS_TOKEN);
*/

val accessToken =
  new DefaultFacebookClient(Version.LATEST).obtainAppAccessToken("365766817503376",
    "64239c3e8c08db610ecda5f62f94f248")


val client = accessToken.getClient

client.fetchObject("me", classOf[User])


