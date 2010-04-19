package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._
import java.util.Date
import java.text.SimpleDateFormat

object GoogleService extends OauthProvider with ContactSource  {
    val service = Service.Google

    val api = :/("www.google.com").secure / "m8" / "feeds"
    val user = api / "contacts"
    val groups = api / "groups"
    val contacts = user
    
    val GetRequestToken = "OAuthGetRequestToken"
    val AuthorizeToken  = "OAuthAuthorizeToken"
    val GetAccessToken  = "OAuthGetAccessToken"
    val VerifierParameter = "oauth_verifier"

    val extras = Map("scope" -> api.to_uri.toString, "oauth_callback" -> (:/(Props.get("host").open_!) / "service" / callback).to_uri.toString)
    val account = :/("www.google.com").secure / "accounts" 

    def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
        val feed = h(user / "default" / "full" <<? Map("max-results" -> 0) <@ (consumer, accessToken) <> identity[scala.xml.Elem])
        Identifier.createIfNeeded( (feed \  "id"              ).text, IdentifierType.GoogleId, self, account)
        Identifier.createIfNeeded(((feed \ "author") \ "name" ).text, IdentifierType.FullName, self, account)
        Identifier.createIfNeeded(((feed \ "author") \ "email").text, IdentifierType.Email   , self, account)
    }
    
    def parseDate(dateString: String) = {
        val parser =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'")
        parser.parse(dateString)
    }
    
    def parseAndStoreContacts(feed: scala.xml.Elem) = {
        println("Google parseAndStoreContacts")
        // for (entry <- (feed \\ "entry")) {
        //     println(entry)
        //     val newContact = Contact.create.owner(User.currentUser.open_!).saveMe
        //     Identifier.createIfNeeded((entry \ "id"   ).text, IdentifierType.GoogleId, newContact, authToken)
        //     Identifier.createIfNeeded((entry \ "title").text, IdentifierType.FullName, newContact, authToken)
        //     for (email <- (entry \\ "email")) {
        //         Identifier.createIfNeeded((email \ "@address").toString, IdentifierType.Email, newContact, authToken)
        //     }
        //     for (phone <- (entry \\ "phoneNumber")) {
        //         Identifier.createIfNeeded(phone.text, IdentifierType.Phone, newContact, authToken)
        //     }
        //     // for (googleGroup <- (entry \\ "groupMembershipInfo")) {
        //     //     val group = Group.findAll(By(Group.id, (googleGroup \ "@href").toString),
        //     //                               By(Group.service, service                      )).head
        //     //     ContactGroup.join(newContact, group)
        //     // }
        // }
    }
    
    def parseAndStoreGroups(feed: scala.xml.Elem) = {
        println("Google parseAndStoreGroups")
        // for (entry <- (feed \\ "entry")) {
        //     val name = (entry \ "title").text
        //     val lastUpdated = parseDate((entry \ "updated").text)
        //     val googleId = (entry \ "id").text
        //     //TODO .account(service) needs to be here and add the current account to the group
        //     Group.create.owner(User.currentUser.open_!).name(name).groupId(googleId).lastUpdated(lastUpdated).save
        // }
    }
    
    def getContacts(accessToken: Token) = {
        h(contacts / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, accessToken) <> parseAndStoreContacts)
    }
    
    def getGroups(accessToken: Token) = {
        h(groups / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, accessToken) <> parseAndStoreGroups)
    }

}