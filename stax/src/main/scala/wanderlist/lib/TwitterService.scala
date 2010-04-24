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

    def parseAndStoreContacts(account: Account)(feed: scala.xml.Elem) = {
        println("Twitter parseAndStoreContacts")
        // for (entry <- (feed \\ "user")) {
        //     println(entry)
        //     val newContact = Contact.create.owner(User.currentUser.open_!).saveMe
        //     Identifier.createIfNeeded((entry \ "id").text         , IdentifierType.TwitterId    , newContact, authToken)
        //     Identifier.createIfNeeded((entry \ "screen_name").text, IdentifierType.TwitterHandle, newContact, authToken)
        //     val name = (entry \ "name").text 
        //     if (name != "") {
        //         Identifier.createIfNeeded(name                    , IdentifierType.FullName     , newContact, authToken)
        //     }
        // }
        updateSpanText("All done! " + 10000000 + " contacts fetched.")
    }
    
    def parseAndStoreGroups(account: Account)(feed: scala.xml.Elem) = {
        println("Twitter parseAndStoreGroups")
    }
    //TODO implement paging for Twitter
    
    def getContacts(account: Account) = {
        h(contacts <@ (consumer, account.token) <> parseAndStoreContacts(account))
    }
    
    def getGroups(account: Account) = {
        val twitterGroups = List("following", "followers")
        for (twitterGroup <- twitterGroups) {
            Group.create.name(twitterGroup)
                        .groupId(twitterGroup)
                        .owner(User.currentUser.open_!)
                        .account(account)
                        .userCreated(false)
        }
        //TODO get lists here!        
    }
}