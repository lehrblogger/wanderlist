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
import scala.xml.{NodeSeq, Node, Elem, Text, Null}
 
 
trait ContactSource { // with Actor?
    val service: Service.Value
    val contacts: dispatch.Request
    val groups: dispatch.Request
    
    var contactCounterName: String = ""
     
    def getAccountData(account: Account, name: String) = { 
        contactCounterName = name
        (new ContactFetcher(account)) ! FetchStart
    }
    
    def updateSpanText(newText: String) = {
        println("updateSpanText!")
        CounterMaster ! CounterUpdate(contactCounterName, newText)
    }
    
    def getGroups(account: Account)
    def parseAndStoreGroups(account: Account)(feed: scala.xml.Elem)
    
    def getContacts(account: Account)
    def parseAndStoreContacts(account: Account, additionalGroups: List[Group])(feed: scala.xml.Elem)
    
    class ContactFetcher(account: Account) extends LiftActor {
        protected def messageHandler = {
            case FetchStart => {
                getGroups(account)
                getContacts(account)
            }
        }
    }
    case object FetchStart
}

