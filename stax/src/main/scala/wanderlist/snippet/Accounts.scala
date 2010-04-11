package wanderlist.snippet 
import wanderlist._
import wanderlist.model._ 
import wanderlist.lib._
import net.liftweb._ 
import http._ 
import SHtml._ 
import S._ 
import js._ 
import JsCmds._ 
import mapper._ 
import util._ 
import Helpers._ 
import scala.xml.{NodeSeq, Text} 
 
class Accounts { 
    def listAccounts(xhtml: NodeSeq) = { 
        val accounts = Account.findAll(By(Account.owner, User.currentUser.open_!))
        accounts.flatMap(account => bind("account", xhtml, "name" -> account.accountId,
                                                           "type" -> account.provider))
    }
}