package wanderlist.lib 

import _root_.net.liftweb.http._ 
import S._ 
import _root_.net.liftweb.util._ 
import Helpers._
import _root_.scala.xml._

import dispatch._
import oauth._
import OAuth._

class GoogleAuth {
    // Google constants
    val GetRequestToken = "OAuthGetRequestToken"
    val AuthorizeToken = "OAuthAuthorizeToken"
    val GetAccessToken = "OAuthGetAccessToken"

    // Google API endpoints
    val account = :/("www.google.com").secure / "accounts"
    val m8 = :/("www.google.com").secure / "m8" / "feeds"
    val contacts = m8 / "contacts"
    val h = new Http
    
    def initiateRequest() = {
        val consumer = Consumer(Props.get("googleConsumer.key")    openOr "", 
                                Props.get("googleConsumer.secret") openOr "")
        val Callback: String = SHtml.link("/oauth_callback", () => {
                            val verifier = java.net.URLDecoder.decode(S.param("oauth_verifier") openOr "", "UTF-8")
                            val accessToken =  h(account / GetAccessToken <@ (consumer, requestToken, verifier) as_token)
                            
                            // Convenient helper method (grab id from xml response)
                            def extractId(feed: scala.xml.Elem): String = (feed \ "id").text

                            // Query for just the user's id (in Google's case, their email address)
                            h(contacts / "default" / "full" <<? Map("max-results" -> 0) <@ (consumer, accessToken) <> extractId)
                             
                        }, Text("")).attribute("href").get.text

        val extras = Map("scope" -> m8.to_uri.toString, "oauth_callback" -> Callback)
        val requestToken = h(account / GetRequestToken << extras <@ consumer as_token)
        val url = (account / AuthorizeToken <<? requestToken to_uri).toString
        S.redirectTo(url)
    }

    // // Convenient helper class
    // case class Contact(name: String, emails: List[String])
    // 
    // // Convenient helper method (grab name, list of emails for each contact in an xml response of contacts)
    // def parse(feed: scala.xml.Elem): List[Contact] =
    //   (for (entry <- feed \\ "entry") yield {
    //     val name = (entry \ "title").text
    //     val emails = (for {
    //       email <- entry \\ "email"
    //       address <- email.attribute("address")
    //     } yield address.text).toList
    //     Contact(name, emails)
    //   }).toList
    // 
    // // Grab names and emails for the first 10 contacts
    // h(contacts / "default" / "full" <<? Map("max-results" -> 10) <@ (consumer, accessToken) <> parse).foreach(println)

}