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
    
    var contactCounterName: String = ""
     
    def getAccountData(accessToken: Token, name: String) = { 
        contactCounterName = name
        (new ContactFetcher(accessToken)) ! FetchStart
    }
    
    def updateSpanText(newText: String) = {
        CounterMaster ! CounterUpdate(contactCounterName, newText)
    }
    
    def getGroups(accessToken: Token)
    def parseAndStoreGroups(feed: scala.xml.Elem)
    
    def getContacts(accessToken: Token)
    def parseAndStoreContacts(feed: scala.xml.Elem)
    
    class ContactFetcher(accessToken: Token) extends LiftActor {
        protected def messageHandler = {
            case FetchStart => {
                //getGroups(accessToken)
                getContacts(accessToken)
            }
        }
    }
    case object FetchStart
}

