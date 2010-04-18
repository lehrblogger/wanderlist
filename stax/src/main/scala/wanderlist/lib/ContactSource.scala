package wanderlist.lib
import scala.actors._ 
import scala.actors.Actor._
import net.liftweb.http.S
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._
import wanderlist.comet._
 
trait ContactSource {
    val service: Service.Value
    val contacts: dispatch.Request
    val groups: dispatch.Request
     
    def getContacts(accessToken: Token, counterSpanId: String) = {
        // val authToken = getAccountForUser(User.currentUser.open_!)
        // val accessToken = Token(authToken.accessTokenValue, authToken.accessTokenSecret)
        // h(contacts <@ (consumer, accessToken) <> parseAndStoreContacts)
        
        val counter = new ContactCounter
        //counter ! counterSpanId
            
        val fetcher = actor {
            loop { 
                react {
                    case FetchStart => {
                        println("let's fetch some contacts!")
                        // for (count <- 0.until(100000000)) { 
                        //     if (count % 10000000 == 0){
                        //         println(count) 
                        //         counter ! count
                        //     }
                        // }
                        counter ! FetchComplete
                    } 
                }
            }
        }
        fetcher ! FetchStart
    }
    //def parseAndStoreContacts()
}
case object FetchStart
