package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

object FoursquareService extends OauthProvider with ContactSource {
    val service = Service.Foursquare
    
    val GetRequestToken = "request_token"
    val AuthorizeToken  = "authorize"
    val GetAccessToken  = "access_token"
    val VerifierParameter = "oauth_token"
    
    val extras = Map.empty[String, String]
    val account = :/("foursquare.com") / "oauth"
    val api = :/("api.foursquare.com") / "v1"
    val contacts = api / "friends"
    val groups = api / ""
    val user = api / "user"
    
    def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
        val feed = h(user <<? Map("count" -> 0) <@ (consumer, accessToken) <> identity[scala.xml.Elem])
        Identifier.createIfNeeded((feed \ "id"       ).text                                 , IdentifierType.FoursquareId , self, account)
        Identifier.createIfNeeded((feed \ "firstname").text + " " + (feed \ "lastname").text, IdentifierType.FullName     , self, account)
        Identifier.createIfNeeded((feed \ "phone"    ).text                                 , IdentifierType.Phone        , self, account)
        Identifier.createIfNeeded((feed \ "email"    ).text                                 , IdentifierType.Email        , self, account)
        Identifier.createIfNeeded((feed \ "twitter"  ).text                                 , IdentifierType.TwitterHandle, self, account)
        Identifier.createIfNeeded((feed \ "facebook" ).text                                 , IdentifierType.FacebookId   , self, account)    
    }
    
    // def parseAndStoreContacts(feed: scala.xml.Elem) = {
    //     for (entry <- (feed \\ "user")) { 
    //         val newContact = Contact.create.owner(User.currentUser.open_!).saveMe
    //         Identifier.createIfNeeded((entry \ "id").text                                           , IdentifierType.FoursquareId, newContact, authToken)
    //         Identifier.createIfNeeded(((entry \ "firstname").text + " " + (entry \ "lastname").text), IdentifierType.FullName,     newContact, authToken)
    //         for (email <- (entry \\ "email")) {
    //             Identifier.createIfNeeded(email.text,    IdentifierType.Email        , newContact, authToken)
    //         }
    //         for (phone <- (entry \\ "phone")) {
    //             Identifier.createIfNeeded(phone.text,    IdentifierType.Phone        , newContact, authToken)
    //         }
    //         for (twitter <- (entry \\ "twitter")) {
    //             Identifier.createIfNeeded(twitter.text,  IdentifierType.TwitterHandle, newContact, authToken)
    //         }
    //         for (facebook <- (entry \\ "facebook")) {
    //             Identifier.createIfNeeded(facebook.text, IdentifierType.FacebookId   , newContact, authToken)
    //         }
    //     }
    // }
    
      
}

