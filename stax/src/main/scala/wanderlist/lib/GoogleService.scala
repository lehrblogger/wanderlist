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

    val m8 = :/("www.google.com").secure / "m8" / "feeds"
    val contacts = m8 / "contacts"
    val groups = m8 / "groups"
    
    val GetRequestToken = "OAuthGetRequestToken"
    val AuthorizeToken  = "OAuthAuthorizeToken"
    val GetAccessToken  = "OAuthGetAccessToken"
    
    val callback = "google_callback"
    val extras = Map("scope" -> m8.to_uri.toString, "oauth_callback" -> (:/(Props.get("host").open_!) / "service" / callback).to_uri.toString)
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
        def parseAndStoreContacts(feed: scala.xml.Elem) = {
            for (entry <- (feed \\ "entry")) {
                val name = (entry \ "title").text
                val lastUpdated = parseDate((entry \ "updated").text)
                val newContact = Contact.create.name(name).owner(User.currentUser.open_!).lastUpdated(lastUpdated).saveMe
                Identifier.createIfNeeded((entry \ "id").text, AuthService.Google, newContact, User.currentUser.open_!, authToken)
                for (email <- (entry \\ "email")) {
                    Identifier.createIfNeeded((email \ "@address").toString, AuthService.Email, newContact, User.currentUser.open_!, authToken)
                }
                for (phone <- (entry \\ "phoneNumber")) {
                    Identifier.createIfNeeded(phone.text, AuthService.Phone, newContact, User.currentUser.open_!, authToken)
                }
                for (googleGroup <- (entry \\ "groupMembershipInfo")) {
                    val group = Group.findAll(By(Group.value, (googleGroup \ "@href").toString),
                                              By(Group.service, provider                      )).head
                    ContactGroup.join(newContact, group)
                }
            }
        }
        
        val authToken = getAuthTokenForUser(User.currentUser.open_!)
        val accessToken = Token(authToken.accessTokenKey, authToken.accessTokenSecret)
        h(contacts / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, accessToken) <> parseAndStoreContacts)
    }

    def extractId(feed: scala.xml.Elem): String = (feed \ "id").text
    
    def getUserId(token: Token) = h(contacts / "default" / "full" <<? Map("max-results" -> 0) <@ (consumer, token) <> extractId)
    
//def getTenKContacts(token: Token) = h(contacts / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, token) <> parse)
}