package wanderlist.lib
import net.liftweb.http.S
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

trait OauthProvider {
    val service: Service.Value
    
    val GetRequestToken: String
    val AuthorizeToken: String
    val GetAccessToken: String
    val VerifierParameter: String
    val extras: Map[String,String]
    val account: dispatch.Request
    val api: dispatch.Request
    val user: dispatch.Request

    val h = new dispatch.Http
    lazy val consumer = oauth.Consumer(
       Props.get(service + "Consumer.value").open_!,
       Props.get(service + "Consumer.secret").open_!)
    lazy val start    = service + "_start"
    lazy val callback = service + "_callback"
    
    def getRequestUrl() = {
        val requestToken = h(account / GetRequestToken << extras <@ consumer as_token)
        TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
        TempToken.create.owner(User.currentUser.open_!).value(requestToken.value).secret(requestToken.secret).save
        (account / AuthorizeToken <<? requestToken to_uri).toString
    }

    def exchangeVerifier(verifier: String) = {
        val tempToken = TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).head
        TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
        val requestToken = new Token(tempToken.value, tempToken.secret)
        h(account / GetAccessToken <@ (consumer, requestToken, verifier) as_token)
    }
    
    def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account)
    
    def initializeAccount(accessToken: Token) = {
        val user = User.currentUser.open_!
        val selfContact = user.selfContact.obj.openOr {
            val c = Contact.create.owner(user).saveMe
            user.selfContact(c).save
            c
        }
        val account = Account.create.owner(User.currentUser.open_!)
                        .accessTokenValue(accessToken.value)
                        .accessTokenSecret(accessToken.secret)
                        .service(service)
                        .authenticated(true)
                        .notes("click here to add notes")
                 .saveMe
        saveIdentifiersForSelf(accessToken, selfContact, account)
        selfContact.save
    }

    def getAccountForUser(user: User) =
        Account.findAll(By(Account.owner, user), By(Account.service, service)).head        

    def getAccessTokenForUser(user: User) = {
        val token = getAccountForUser(user)
        Token(token.accessTokenValue, token.accessTokenSecret)
    }
}
