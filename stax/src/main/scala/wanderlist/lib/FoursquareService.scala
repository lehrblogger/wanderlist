package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

object FoursquareService extends OAuthProvider {
  val provider = AuthService.Foursquare

  val GetRequestToken = "request_token"
  val AuthorizeToken  = "authorize"
  val GetAccessToken  = "access_token"
  
  val callback = "foursquare_callback"
  val extras = Map.empty[String, String]
  val account = :/("foursquare.com") / "oauth"
}

