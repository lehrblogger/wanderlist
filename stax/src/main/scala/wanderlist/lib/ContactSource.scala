package wanderlist.lib
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
     
    def getContacts(accessToken: Token) = {
         val user = User.currentUser.open_!
         val fetcher = new ContactFetcher
         fetcher ! service
         
         // val authToken = getAccountForUser(User.currentUser.open_!)
         // val accessToken = Token(authToken.accessTokenValue, authToken.accessTokenSecret)
         // h(contacts <@ (consumer, accessToken) <> parseAndStoreContacts)
         
    }
         
    //def parseAndStoreContacts()
}
