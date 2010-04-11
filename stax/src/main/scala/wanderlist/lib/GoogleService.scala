package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._
import java.util.Date
import java.text.SimpleDateFormat

object GoogleService extends OAuthProvider {
    val provider = AuthService.Google

    val api = :/("www.google.com").secure / "m8" / "feeds"
    val contacts = api / "contacts"
    val groups = api / "groups"
    
    val GetRequestToken = "OAuthGetRequestToken"
    val AuthorizeToken  = "OAuthAuthorizeToken"
    val GetAccessToken  = "OAuthGetAccessToken"

    val extras = Map("scope" -> api.to_uri.toString, "oauth_callback" -> (:/(Props.get("host").open_!) / "service" / callback).to_uri.toString)
    val account = :/("www.google.com").secure / "accounts" 

    def parseDate(dateString: String) = {
        val parser =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'")
        parser.parse(dateString)
    }

    def getGroups() = {
      def parseAndStoreGroups(feed: scala.xml.Elem) =
          for (entry <- (feed \\ "entry")) {
              val name = (entry \ "title").text
              val lastUpdated = parseDate((entry \ "updated").text)
              val googleId = (entry \ "id").text
              Group.create.owner(User.currentUser.open_!).service(provider).name(name).value(googleId).lastUpdated(lastUpdated).save
          }
      val accessToken = getTokenForUser(User.currentUser.open_!)
      h(groups / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, accessToken) <> parseAndStoreGroups)
    }

    def getContacts() = {
        val authToken = getAuthTokenForUser(User.currentUser.open_!)
        def parseAndStoreContacts(feed: scala.xml.Elem) = {
            for (entry <- (feed \\ "entry")) {
                val newContact = Contact.create.owner(User.currentUser.open_!).saveMe
                Identifier.createIfNeeded((entry \ "id"   ).text, IdentifierType.GoogleId, newContact, User.currentUser.open_!, authToken)
                Identifier.createIfNeeded((entry \ "title").text, IdentifierType.FullName, newContact, User.currentUser.open_!, authToken)
                for (email <- (entry \\ "email")) {
                    Identifier.createIfNeeded((email \ "@address").toString, IdentifierType.Email, newContact, User.currentUser.open_!, authToken)
                }
                for (phone <- (entry \\ "phoneNumber")) {
                    Identifier.createIfNeeded(phone.text, IdentifierType.Phone, newContact, User.currentUser.open_!, authToken)
                }
                for (googleGroup <- (entry \\ "groupMembershipInfo")) {
                    val group = Group.findAll(By(Group.value, (googleGroup \ "@href").toString),
                                              By(Group.service, provider                      )).head
                    ContactGroup.join(newContact, group)
                }
            }
        }
        val accessToken = Token(authToken.accessTokenKey, authToken.accessTokenSecret)
        h(contacts / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, accessToken) <> parseAndStoreContacts)
    }

    def extractId(feed: scala.xml.Elem): String = (feed \ "id").text
    
    def getUserId(token: Token) = h(contacts / "default" / "full" <<? Map("max-results" -> 0) <@ (consumer, token) <> extractId)
    
//def getTenKContacts(token: Token) = h(contacts / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, token) <> parse)
}