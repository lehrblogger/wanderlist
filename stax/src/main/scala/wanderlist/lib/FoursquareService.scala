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
  val VerifierParameter = "oauth_token"
  
  
  val extras = Map.empty[String, String]
  val account = :/("foursquare.com") / "oauth"
  val api = :/("api.foursquare.com") / "v1"
  val contacts = api / "friends"
  val userInfo = api / "user"
  
  override def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
      val feed = h(userInfo <<? Map("count" -> 0) <@ (consumer, accessToken) <> identity[scala.xml.Elem])
      Identifier.createIfNeeded((feed \ "id"       ).text                                 , IdentifierType.FoursquareId, self, User.currentUser.open_!, account)
      Identifier.createIfNeeded((feed \ "firstname").text + " " + (feed \ "lastname").text, IdentifierType.FullName    , self, User.currentUser.open_!, account)
      Identifier.createIfNeeded((feed \ "phone"    ).text                                 , IdentifierType.Phone       , self, User.currentUser.open_!, account)
      Identifier.createIfNeeded((feed \ "email"    ).text                                 , IdentifierType.Email       , self, User.currentUser.open_!, account)
      Identifier.createIfNeeded((feed \ "twitter"  ).text                                 , IdentifierType.TwitterId   , self, User.currentUser.open_!, account)
      Identifier.createIfNeeded((feed \ "facebook" ).text                                 , IdentifierType.FacebookId  , self, User.currentUser.open_!, account)    
  }
  
  def getContacts() = {
      val authToken = getAccountForUser(User.currentUser.open_!)
      def parseAndStoreContacts(feed: scala.xml.Elem) = {
          for (entry <- (feed \\ "user")) { 
              val newContact = Contact.create.owner(User.currentUser.open_!).saveMe
              Identifier.createIfNeeded((entry \ "id").text                                           , IdentifierType.FoursquareId, newContact, User.currentUser.open_!, authToken)
              Identifier.createIfNeeded(((entry \ "firstname").text + " " + (entry \ "lastname").text), IdentifierType.FullName,     newContact, User.currentUser.open_!, authToken)
              for (email <- (entry \\ "email")) {
                  Identifier.createIfNeeded(email.text,    IdentifierType.Email        , newContact, User.currentUser.open_!, authToken)
              }
              for (phone <- (entry \\ "phone")) {
                  Identifier.createIfNeeded(phone.text,    IdentifierType.Phone        , newContact, User.currentUser.open_!, authToken)
              }
              for (twitter <- (entry \\ "twitter")) {
                  Identifier.createIfNeeded(twitter.text,  IdentifierType.TwitterHandle, newContact, User.currentUser.open_!, authToken)
              }
              for (facebook <- (entry \\ "facebook")) {
                  Identifier.createIfNeeded(facebook.text, IdentifierType.FacebookId   , newContact, User.currentUser.open_!, authToken)
              }
          }
      }
      val accessToken = Token(authToken.accessTokenValue, authToken.accessTokenSecret)
      h(contacts <@ (consumer, accessToken) <> parseAndStoreContacts)
  }
      
}

