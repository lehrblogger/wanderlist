package wanderlist.lib 

import _root_.net.liftweb.http._ 
import S._ 
import _root_.net.liftweb.util._ 
import Helpers._
import _root_.scala.xml._

import dispatch._
import oauth._
import OAuth._

import net.liftweb._ 
import mapper._
 
import wanderlist.model._

class GoogleAuth {
    // Google constants
    val GetRequestToken = "OAuthGetRequestToken"
    val AuthorizeToken  = "OAuthAuthorizeToken"
    val GetAccessToken  = "OAuthGetAccessToken"

    // Google API endpoints
    val account = :/("www.google.com").secure / "accounts"
    val m8 = :/("www.google.com").secure / "m8" / "feeds"
    val contacts = m8 / "contacts"
    
    val h = new Http
    val consumer = Consumer(Props.get("googleConsumer.key").open_!, 
                            Props.get("googleConsumer.secret").open_!)

    def initiateRequest() = {
        var requestToken: Token = null
        val Callback: String = SHtml.link(Props.get("host").open_! + "/oauth_callback", () => {
                            val verifier = java.net.URLDecoder.decode(S.param("oauth_verifier").open_!, "UTF-8")
                            val accessToken =  h(account / GetAccessToken <@ (consumer, requestToken, verifier) as_token)    
                            ContactProvider.create.authenticated(true).owner(User.currentUser.open_!).accessTokenKey(accessToken.value).accessTokenSecret(accessToken.secret).save                            
                            
                            
                            
                            S.redirectTo(Props.get("host").open_!)
                        }, Text("")).attribute("href").get.text
        val extras = Map("scope" -> m8.to_uri.toString, "oauth_callback" -> Callback)
        requestToken = h(account / GetRequestToken << extras <@ consumer as_token)
        val url = (account / AuthorizeToken <<? requestToken to_uri).toString
        S.redirectTo(url)   
    }

    def getTokenForUser(user: User) = {
        val cp = ContactProvider.findAll(By(ContactProvider.owner, user)).head
        Token(cp.accessTokenKey, cp.accessTokenSecret)
    }
    
    // Convenient helper method (grab id from xml response)
    def extractId(feed: scala.xml.Elem): String = (feed \ "id").text

    // Convenient helper class
    case class Contact(name: String, emails: List[String])

    // Convenient helper method (grab name, list of emails for each contact in an xml response of contacts)
    def parse(feed: scala.xml.Elem): List[Contact] =
      (for (entry <- feed \\ "entry") yield {
        val name = (entry \ "title").text
        val emails = (for {
          email <- entry \\ "email"
          address <- email.attribute("address")
        } yield address.text).toList
        Contact(name, emails)
      }).toList


      object name extends MappedPoliteString(this, 256)
      object googleId extends MappedPoliteString(this, 256)
      object owner extends MappedLongForeignKey(this, User)
      object lastUpdated extends MappedLongForeignKey(this, User)
      object groups extends HasManyThrough(this, Group, ContactGroup, ContactGroup.contact, Contactgroup.group)
      
    def getUserId(token: Token) = h(contacts / "default" / "full" <<? Map("max-results" -> 0) <@ (consumer, token) <> extractId)

    def getTenKContacts(token: Token) = h(contacts / "default" / "full" <<? Map("max-results" -> 10000) <@ (consumer, token) <> parse)
}