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
        Account.findAll(By(Account.owner, User.currentUser.open_!)).flatMap(
            account => bind("account", xhtml, 
                "type" -> account.provider,
                "identifiers" -> IdentifierAccount.findAll(By(IdentifierAccount.account, account)).flatMap(
                    indentifierAccount => bind("ident", chooseTemplate("identifier", "list", xhtml), 
                        "value" -> indentifierAccount.identifier.obj.open_!.value
                    )
                ),
                "notes" -> account.notes
            )
        )
    }
}