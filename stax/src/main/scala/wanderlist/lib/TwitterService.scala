package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

object TwitterService extends OAuthProvider {
  val provider = AuthService.Foursquare
  
  val GetRequestToken = "request_token"
  val AuthorizeToken  = "authorize"
  val GetAccessToken  = "access_token"
  
  val extras = Map.empty[String, String]
  val account = :/("twitter.com") / "oauth"
  
  
  val api = :/("api.foursquare.com") / "v1"
  val contacts = api / "friends"
  
  def getContacts() = {
      val authToken = getAuthTokenForUser(User.currentUser.open_!)
      def parseAndStoreContacts(feed: scala.xml.Elem) = {
          for (entry <- (feed \\ "user")) {
              val newContact = Contact.create.name(name).owner(User.currentUser.open_!).saveMe//.lastUpdated(lastUpdated)
              Identifier.createIfNeeded((entry \ "id").text,   AuthService.Twitter, newContact, User.currentUser.open_!, authToken)
              Identifier.createIfNeeded((entry \ "name").text, AuthService.Name,    newContact, User.currentUser.open_!, authToken)
              Identifier.createIfNeeded((entry \ "name").text, AuthService.Name,    newContact, User.currentUser.open_!, authToken)

              
              
                    val name = (entry \ "firstname").text + " " + (entry \ "lastname").text
              for (email <- (entry \\ "email")) {
                  Identifier.createIfNeeded(email.text,    AuthService.Email,    newContact, User.currentUser.open_!, authToken)
              }
              for (phone <- (entry \\ "phone")) {
                  Identifier.createIfNeeded(phone.text,    AuthService.Phone,    newContact, User.currentUser.open_!, authToken)
              }
              for (twitter <- (entry \\ "twitter")) {
                  Identifier.createIfNeeded(twitter.text,  AuthService.Twitter,  newContact, User.currentUser.open_!, authToken)
              }
              for (facebook <- (entry \\ "facebook")) {
                  Identifier.createIfNeeded(facebook.text, AuthService.Facebook, newContact, User.currentUser.open_!, authToken)
              }
          }
      }
      val accessToken = Token(authToken.accessTokenKey, authToken.accessTokenSecret)
      h(contacts <@ (consumer, accessToken) <> parseAndStoreContacts)
  }
      
}

