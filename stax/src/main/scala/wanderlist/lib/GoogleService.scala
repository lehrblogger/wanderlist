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
    val VerifierParameter = "oauth_verifier"

    val extras = Map("scope" -> api.to_uri.toString, "oauth_callback" -> (:/(Props.get("host").open_!) / "service" / callback).to_uri.toString)
    val account = :/("www.google.com").secure / "accounts" 

    override def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
        def parseAndStoreSelfInfo(feed: scala.xml.Elem) = {
            Identifier.createIfNeeded( (feed \  "id"              ).text, IdentifierType.GoogleId, self, User.currentUser.open_!, account)
            Identifier.createIfNeeded(((feed \ "author") \ "name" ).text, IdentifierType.FullName, self, User.currentUser.open_!, account)
            Identifier.createIfNeeded(((feed \ "author") \ "email").text, IdentifierType.Email   , self, User.currentUser.open_!, account)
        }
        h(contacts / "default" / "full" <<? Map("max-results" -> 0) <@ (consumer, accessToken) <> parseAndStoreSelfInfo)
    }
    
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
              //TODO .account(provider) needs to be here and add the current account to the group
              Group.create.owner(User.currentUser.open_!).name(name).groupId(googleId).lastUpdated(lastUpdated).save
          }
      val accessToken = getAccessTokenForUser(User.currentUser.open_!)
      h(groups / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, accessToken) <> parseAndStoreGroups)
    }

    def getContacts() = {
        val authToken = getAccountForUser(User.currentUser.open_!)
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
                // for (googleGroup <- (entry \\ "groupMembershipInfo")) {
                //     val group = Group.findAll(By(Group.id, (googleGroup \ "@href").toString),
                //                               By(Group.service, provider                      )).head
                //     ContactGroup.join(newContact, group)
                // }
            }
        }
        val accessToken = Token(authToken.accessTokenValue, authToken.accessTokenSecret)
        h(contacts / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, accessToken) <> parseAndStoreContacts)
    }
}