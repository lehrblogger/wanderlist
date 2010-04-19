package wanderlist.lib
import net.liftweb.http.S
import net.liftweb.mapper._
import net.liftweb.util.Props
import net.liftweb.actor._
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._
import wanderlist.comet._
 
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.js._
import _root_.net.liftweb.http.js.JsCmds._
import scala.xml._
 
 
trait ContactSource { // with Actor?
    val service: Service.Value
    val contacts: dispatch.Request
    val groups: dispatch.Request
     
    def getContacts(accessToken: Token, contactCounterName: String) = { 
        // val authToken = getAccountForUser(User.currentUser.open_!)
        // val accessToken = Token(authToken.accessTokenValue, authToken.accessTokenSecret)
        // h(contacts <@ (consumer, accessToken) <> parseAndStoreContacts)
        
        (new ContactFetcher(accessToken, contactCounterName)) ! FetchStart
    }
    //def parseAndStoreContacts()
}

class ContactFetcher(accessToken: Token, contactCounterName: String) extends LiftActor {
    protected def messageHandler = {
        case FetchStart => {
            def updateSpanText(newText: String) = CounterMaster ! CounterUpdate(contactCounterName, newText)
            var count = 0
            for (i <- 0.until(10)) { 
                count = i
                Thread.sleep(5000)
                updateSpanText(count.toString + " contacts fetched...")
            }
            updateSpanText("All done! " + count + " contacts fetched.")
        }
    }
}
case object FetchStart