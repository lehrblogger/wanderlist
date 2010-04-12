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
    private def desc(account: Account, reDraw: () => JsCmd) = 
        swappable(<span>{account.notes}</span>, <span>{ajaxText(account.notes, v => {account.notes(v).save; reDraw()})}</span>)

    private def doList(reDraw: () => JsCmd)(xhtml: NodeSeq): NodeSeq = {
        Account.findAll(By(Account.owner, User.currentUser.open_!)).flatMap(account =>
            bind("account", xhtml, 
                "type" -> account.provider,
                "identifiers" -> IdentifierAccount.findAll(By(IdentifierAccount.account, account)).flatMap(
                    indentifierAccount => bind("ident", chooseTemplate("identifier", "list", xhtml), 
                        "value" -> indentifierAccount.identifier.obj.open_!.value
                    )
                ),
                "notes" -> desc(account, reDraw)
            )
        )
    }

    def list(xhtml: NodeSeq) = { 
        val id = S.attr("all_id").open_! 

        def inner(): NodeSeq = { 
            def reDraw() = SetHtml(id, inner())
            bind("account", xhtml, "list"    -> doList(reDraw)_ )
        }

        inner()
    }
}