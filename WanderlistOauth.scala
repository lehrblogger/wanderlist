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
val groups = m8 / "groups"

// Stuff you might want to customize
val consumer = Consumer("YOUR_KEY", "YOUR_SECRET")
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
// Convenient helper class

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
  
def parse2(feed: scala.xml.Elem) = feed.toList


case class Group(name: String, googleID: String, lastUpdated: String)
{
  if link.attribute("rel") == "edit" {
      val googleId <- (entry \\ "link").attribute("href")
      break
  }
}

 
object name extends MappedPoliteString(this, 256)
object googleId extends MappedPoliteString(this, 256)
object owner extends MappedLongForeignKey(this, User)
object lastUpdated extends MappedDateTime(this)
object groups extends HasManyThrough(this, Group, ContactGroup, ContactGroup.contact, Contactgroup.group)



case class Contact(name: String,  googleID: String, lastUpdated: String, emails: List[String], groups: List[String])
def parseAndStoreContacts(feed: scala.xml.Elem): List[Contact] =
  (for (entry <- feed \\ "entry") yield {
    val name = (entry \ "title").text
    val lastUpdated = (entry \ "updated").text
    var googleId = "ERROR"
    for (link <- entry \\ "link") { 
        if ((link \ "@rel") == "edit") googleId = (link \ "@href").toString
    }
    val emails = (for {
      email <- entry \\ "email"
      address <- email.attribute("address")
    } yield address.text).toList
    val groups = (for {
      group <- entry \\ "groupMembershipInfo"
      link <- group \ "@href"
    } yield link.text).toList
    Contact(name, googleId, lastUpdated, emails, groups)
  }).toList
h(contacts / "default" / "full" <<? Map("max-results" -> 2) <@ (consumer, accessToken) <> parseAndStoreContacts).foreach(println)
  
case class Group(name: String, googleID: String, lastUpdated: String)
def parseAndStoreGroups(feed: scala.xml.Elem): List[Group] =
  (for (entry <- feed \\ "entry") yield {
    val name = (entry \ "title").text
    val lastUpdated = (entry \ "updated").text
    (entry \\ "link").find(link => (link \ "@rel") == "edit") match {
        case Some(link) => Group(name, (link \ "@href").toString, lastUpdated)
        case None => error("googleId link found")
    }
  }).toList
h(groups / "default" / "full" <<? Map("max-results" -> 1) <@ (consumer, accessToken) <> parseAndStoreGroups).foreach(println)












