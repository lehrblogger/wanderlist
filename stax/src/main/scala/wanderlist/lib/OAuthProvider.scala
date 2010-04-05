package wanderlist.lib

import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._
import java.util.Date
import java.text.SimpleDateFormat

trait OAuthProvider {
    val provider: AuthService.Value

    val GetRequestToken: String
    val AuthorizeToken: String
    val GetAccessToken: String
    val callback: String
    val extras: Map[String,String]
    val account: dispatch.Request
    
    val h = new dispatch.Http
    lazy val consumer = oauth.Consumer(
       Props.get(provider + "Consumer.key").open_!,
       Props.get(provider + "Consumer.secret").open_!)

    def getRequestUrl() = {
        val requestToken = h(account / GetRequestToken << extras <@ consumer as_token)
        TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
        TempToken.create.owner(User.currentUser.open_!).key(requestToken.value).secret(requestToken.secret).save
        (account / AuthorizeToken <<? requestToken to_uri).toString
    }

    def exchangeToken(verifier: String) = {
        val tempToken = TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).head
        TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
        val requestToken = new Token(tempToken.key, tempToken.secret)
        val accessToken = h(account / GetAccessToken <@ (consumer, requestToken, verifier) as_token)
        AuthToken.create.authenticated(true)
                        .owner(User.currentUser.open_!)
                        .accessTokenKey(accessToken.value)
                        .accessTokenSecret(accessToken.secret)
                        .provider(provider)
                 .saveMe
    }
  
    def getTokenForUser(user: User) = {
        val token = AuthToken.findAll(By(AuthToken.owner, user), By(AuthToken.provider, provider)).head
        Token(token.accessTokenKey, token.accessTokenSecret)
    }
}

object FoursquareService extends OAuthProvider {
  val provider = AuthService.Foursquare

  val GetRequestToken = "request_token"
  val AuthorizeToken  = "authorize"
  val GetAccessToken  = "access_token"
  
  val callback = "foursquare_callback"
  val extras = Map.empty[String, String]
  val account = :/("foursquare.com") / "oauth"
}

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
              var googleId = (entry \ "id").text
              Group.create.name(name).owner(User.currentUser.open_!).googleId(googleId).lastUpdated(lastUpdated).save
          }
      val accessToken = getTokenForUser(User.currentUser.open_!)
      h(groups / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, accessToken) <> parseAndStoreGroups)
    }

    def getContacts() = {
        def parseAndStoreContacts(feed: scala.xml.Elem) = {
            for (entry <- (feed \\ "entry")) {
                val name = (entry \ "title").text
                val lastUpdated = parseDate((entry \ "updated").text)
                var googleId = (entry \ "id").text
                val newContact = Contact.create.name(name).owner(User.currentUser.open_!).googleId(googleId).lastUpdated(lastUpdated)
                newContact.save  
                for (email <- (entry \\ "email")) {
                    ContactEmail.create.email((email \ "@address").toString).contact(newContact).save
                }
                for (googleGroup <- (entry \\ "groupMembershipInfo")) {
                    //println((group \ "@href").toString)
                    val group = Group.findAll(By(Group.googleId, (googleGroup \ "@href").toString)).head
                    ContactGroup.join(newContact, group)
                }
            }
        }
        val accessToken = getTokenForUser(User.currentUser.open_!)
        h(contacts / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, accessToken) <> parseAndStoreContacts)
    }

    def extractId(feed: scala.xml.Elem): String = (feed \ "id").text
    
    def getUserId(token: Token) = h(contacts / "default" / "full" <<? Map("max-results" -> 0) <@ (consumer, token) <> extractId)
    
//def getTenKContacts(token: Token) = h(contacts / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, token) <> parse)
}
