package wanderlist.lib

import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

trait OAuthProvider {
  val provider: AuthProvider.Value

  val GetRequestToken: String
  val AuthorizeToken: String
  val GetAccessToken: String
  val account: dispatch.Request

  val h = new dispatch.Http
  val consumer = oauth.Consumer(
    Props.get(provider + "Consumer.key").open_!,
    Props.get(provider + "Consumer.secret").open_!)

  def initiateRequest() = {
    val requestToken = h(account / GetRequestToken <@ consumer as_token)
    TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
    TempToken.create.owner(User.currentUser.open_!).key(requestToken.value).secret(requestToken.secret).save
    account / AuthorizeToken <<? requestToken to_uri
  }

  def exchangeToken(verifier: String) = {
    val tempToken = TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).head
    TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
    val requestToken = new Token(tempToken.key, tempToken.secret)
    val accessToken = h(account / GetAccessToken <@ (consumer, requestToken, verifier) as_token)
    AuthToken.create.authenticated(true).owner(User.currentUser.open_!).accessTokenKey(accessToken.value).accessTokenSecret(accessToken.secret).saveMe()
  }

  def getTokenForUser(user: User) = {
    val token = AuthToken.findAll(By(AuthToken.owner, user), By(AuthToken.provider, provider)).head
    Token(token.accessTokenKey, token.accessTokenSecret)
  }
}

object FoursquareProvider extends OAuthProvider {
  val provider = AuthProvider.Foursquare

  val GetRequestToken = "request_token"
  val AuthorizeToken  = "authorize"
  val GetAccessToken  = "access_token"
  val account = :/("foursquare.com") / "oauth"
}

object GoogleProvider extends OAuthProvider {
  val provider = AuthProvider.Google

  val GetRequestToken = "OAuthGetRequestToken"
  val AuthorizeToken  = "OAuthAuthorizeToken"
  val GetAccessToken  = "OAuthGetAccessToken"
  val account = :/("www.google.com").secure / "accounts"

  // val m8 = :/("www.google.com").secure / "m8" / "feeds"
  // val contacts = m8 / "contacts"
  // val groups = m8 / "groups"
}
