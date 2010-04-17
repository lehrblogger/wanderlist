package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

object TwitterService extends OauthProvider with ContactSource  {
    val service = Service.Twitter
    
    val GetRequestToken = "request_token"
    val AuthorizeToken  = "authorize"
    val GetAccessToken  = "access_token"
    val VerifierParameter = "oauth_token"
    
    val extras = Map.empty[String, String]
    val account = :/("twitter.com") / "oauth"
    
    val api = :/("api.twitter.com") / "1"
    val contacts = api / "statuses" / "friends.xml"
    val groups = api / ""
    val user = api / "statuses" / "user_timeline.xml"
    
    def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
        val feed = h(user <<? Map("count" -> 0) <@ (consumer, accessToken) <> identity[scala.xml.Elem])
        val userXml = ((feed \\ "status") \ "user")
        Identifier.createIfNeeded((userXml \ "id"         ).text, IdentifierType.TwitterId    , self, account)
        Identifier.createIfNeeded((userXml \ "screen_name").text, IdentifierType.TwitterHandle, self, account)
        Identifier.createIfNeeded((userXml \ "name"       ).text, IdentifierType.FullName     , self, account) 
    }
    // 
    // override def getContacts() = {
    //     val authToken = getAccountForUser(User.currentUser.open_!)
    //     def parseAndStoreContacts(feed: scala.xml.Elem) = {
    //         for (entry <- (feed \\ "user")) {
    //             val newContact = Contact.create.owner(User.currentUser.open_!).saveMe
    //             Identifier.createIfNeeded((entry \ "id").text         , IdentifierType.TwitterId    , newContact, authToken)
    //             Identifier.createIfNeeded((entry \ "screen_name").text, IdentifierType.TwitterHandle, newContact, authToken)
    //             val name = (entry \ "name").text 
    //             if (name != "") {
    //                 Identifier.createIfNeeded(name                    , IdentifierType.FullName     , newContact, authToken)
    //             }
    //         }
    //     }
    //     val accessToken = Token(authToken.accessTokenValue, authToken.accessTokenSecret)
    //     h(contacts <@ (consumer, accessToken) <> parseAndStoreContacts)
    //     //TODO implement paging for Twitter
    // }
}