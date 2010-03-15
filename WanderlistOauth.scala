import dispatch._
import oauth._
import OAuth._

// Google constants
val GetRequestToken = "OAuthGetRequestToken"
val AuthorizeToken = "OAuthAuthorizeToken"
val GetAccessToken = "OAuthGetAccessToken"

// Google API endpoints
val account = :/("www.google.com").secure / "accounts"
val m8 = :/("www.google.com").secure / "m8" / "feeds"
val contacts = m8 / "contacts"

// Stuff you might want to customize
val consumer = Consumer(key, pass)
val Callback = "http://localhost:8080/callback"

// The three legs of the OAuth exchange
val h = new Http
val extras = Map("scope" -> m8.to_uri.toString, "oauth_callback" -> Callback)
val requestToken = h(account / GetRequestToken << extras <@ consumer as_token)
val accessToken = {
  println("Please visit:")
  println(account / AuthorizeToken <<? requestToken to_uri)
  val verifier = java.net.URLDecoder.decode(Console.readLine("Enter verifier: "), "UTF-8")
  h(account / GetAccessToken <@ (consumer, requestToken, verifier) as_token)
}

// Convenient helper method (grab id from xml response)
def extractId(feed: scala.xml.Elem): String =
  (feed \ "id").text

// Query for just the user's id (in Google's case, their email address)
h(contacts / "default" / "full" <<? Map("max-results" -> 0) <@ (consumer, accessToken) <> extractId)

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

// Grab names and emails for the first 10 contacts
h(contacts / "default" / "full" <<? Map("max-results" -> 10) <@ (consumer, accessToken) <> parse).foreach(println)
