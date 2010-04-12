package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

object TwitterService extends OAuthProvider {
    val provider = AuthService.Twitter
    
    val GetRequestToken = "request_token"
    val AuthorizeToken  = "authorize"
    val GetAccessToken  = "access_token"
    val VerifierParameter = "oauth_token"
    
    val extras = Map.empty[String, String]
    val account = :/("twitter.com") / "oauth"
    
    val api = :/("api.twitter.com") / "1"
    val contacts = api / "statuses" / "friends.xml"
    val userInfo = api / "statuses" / "user_timeline.xml"
    
    override def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
        val feed = h(userInfo <<? Map("count" -> 0) <@ (consumer, accessToken) <> identity[scala.xml.Elem])
        val user = ((feed \\ "status") \ "user")
        Identifier.createIfNeeded((user \ "id"         ).text, IdentifierType.TwitterId    , self, User.currentUser.open_!, account)
        Identifier.createIfNeeded((user \ "screen_name").text, IdentifierType.TwitterHandle, self, User.currentUser.open_!, account)
        Identifier.createIfNeeded((user \ "name"       ).text, IdentifierType.FullName     , self, User.currentUser.open_!, account) 
    }
    
    def getContacts() = {
        val authToken = getAccountForUser(User.currentUser.open_!)
        def parseAndStoreContacts(feed: scala.xml.Elem) = {
            for (entry <- (feed \\ "user")) {
                val newContact = Contact.create.owner(User.currentUser.open_!).saveMe
                Identifier.createIfNeeded((entry \ "id").text         , IdentifierType.TwitterId    , newContact, User.currentUser.open_!, authToken)
                Identifier.createIfNeeded((entry \ "screen_name").text, IdentifierType.TwitterHandle, newContact, User.currentUser.open_!, authToken)
                val name = (entry \ "name").text 
                if (name != "") {
                    Identifier.createIfNeeded(name                    , IdentifierType.FullName     , newContact, User.currentUser.open_!, authToken)
                }
            }
        }
        val accessToken = Token(authToken.accessTokenValue, authToken.accessTokenSecret)
        h(contacts <@ (consumer, accessToken) <> parseAndStoreContacts)
        //TODO implement paging for Twitter
    }
}