package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

object FoursquareService extends OAuthProvider {
  val provider = AuthService.Foursquare

  val GetRequestToken = "request_token"
  val AuthorizeToken  = "authorize"
  val GetAccessToken  = "access_token"
  
  val extras = Map.empty[String, String]
  val account = :/("foursquare.com") / "oauth"
  val api = :/("api.foursquare.com") / "v1"
  val contacts = api / "friends"
  
  def getContacts() = {
      val authToken = getAuthTokenForUser(User.currentUser.open_!)
      def parseAndStoreContacts(feed: scala.xml.Elem) = {
          for (entry <- (feed \\ "user")) {
              //val lastUpdated = " " //TODO make this the current date/time
              val newContact = Contact.create.name(name).owner(User.currentUser.open_!).saveMe//.lastUpdated(lastUpdated)
              Identifier.createIfNeeded((entry \ "id").text,                                            AuthService.Foursquare, newContact, User.currentUser.open_!, authToken)
              Identifier.createIfNeeded(((entry \ "firstname").text + " " + (entry \ "lastname").text), AuthService.Namw,       newContact, User.currentUser.open_!, authToken)
              for (email <- (entry \\ "email")) {
                  Identifier.createIfNeeded(email.text,    AuthService.Email,    newContact, User.currentUser.open_!, authToken)
              }
              for (phone <- (entry \\ "phone")) {
                  Identifier.createIfNeeded(phone.text,    AuthService.Phone,    newContact, User.currentUser.open_!, authToken)
              }
              for (twitter <- (entry \\ "twitter")) {
                  Identifier.createIfNeeded(twitter.text,  AuthService.Name,     newContact, User.currentUser.open_!, authToken)
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

