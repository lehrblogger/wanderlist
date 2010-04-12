package wanderlist.lib
import net.liftweb.http.S
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._
trait OAuthProvider {
    val provider: AuthService.Value

    val GetRequestToken: String
    val AuthorizeToken: String
    val GetAccessToken: String
    val VerifierParameter: String
    val extras: Map[String,String]
    val account: dispatch.Request
    val api: dispatch.Request

    val h = new dispatch.Http
    lazy val consumer = oauth.Consumer(
       Props.get(provider + "Consumer.key").open_!,
       Props.get(provider + "Consumer.secret").open_!)
    lazy val start    = provider + "_start"
    lazy val callback = provider + "_callback"
    
    def getRequestUrl() = {
        val requestToken = h(account / GetRequestToken << extras <@ consumer as_token)
        TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
        TempToken.create.owner(User.currentUser.open_!).key(requestToken.value).secret(requestToken.secret).save
        (account / AuthorizeToken <<? requestToken to_uri).toString
    }

    def exchangeToken(verifier: String) = {
        val tempToken = TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).head
        TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
        val requestToken = new Token(tempToken.key, tempToken.secret)
        val accessToken = h(account / GetAccessToken <@ (consumer, requestToken, verifier) as_token)
        Account.create.authenticated(true)
                        .owner(User.currentUser.open_!)
                        .accessTokenKey(accessToken.value)
                        .accessTokenSecret(accessToken.secret)
                        .provider(provider)
                 .saveMe
    }

    def getAccountForUser(user: User) =
        Account.findAll(By(Account.owner, user), By(Account.provider, provider)).head        

    def getAccessTokenForUser(user: User) = {
        val token = getAccountForUser(user)
        Token(token.accessTokenKey, token.accessTokenSecret)
    }
}