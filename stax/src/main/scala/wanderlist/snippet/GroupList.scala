package wanderlist.snippet 
import wanderlist._
import wanderlist.model._ 
import wanderlist.lib._
import wanderlist.comet._
import net.liftweb._ 
import http._ 
import SHtml._ 
import S._ 
import js._ 
import JsCmds._ 
import mapper._ 
import util._ 
import Helpers._ 
import scala.xml.{NodeSeq, Text, UnprefixedAttribute, Null} 
import dispatch.oauth._
 
class GroupList { 
    private def desc(account: Account, reDraw: () => JsCmd) = 
        swappable(<span>{account.notes}</span>, <span>{ajaxText(account.notes, v => {account.notes(v).save; reDraw()})}</span>)

    private def identifiersToShow(account: Account) = {
        val selfContact = account.owner.obj.open_!.selfContact.obj.open_!
        Identifier.findAll(By(Identifier.contact, selfContact)).filter(identifier => {
            IdentifierAccount.findAll(By(IdentifierAccount.identifier, identifier), By(IdentifierAccount.account, account)) match {
                case List(identifierAccount) => true
                case _                       => false
            }
        })
    }
        
    private def doList(reDraw: () => JsCmd)(xhtml: NodeSeq): NodeSeq = {
        Account.findAll(By(Account.owner, User.currentUser.open_!)).flatMap(account => {
            bind("account", xhtml, 
                "type" -> account.service,
                "notes" -> desc(account, reDraw),
                "groups" -> Group.findAll(By(Group.account, account)).flatMap(group => 
                    bind("g", chooseTemplate("group", "list", xhtml),
                        "name" -> group.name,
                        "size" -> group.contacts.length
                    )
                )
            )
        })
    }
    
    def list(xhtml: NodeSeq) = { 
        val id = S.attr("all_id").open_! 

        def inner(): NodeSeq = { 
            def reDraw() = SetHtml(id, inner())
            bind("accountList", xhtml, "list"    -> doList(reDraw)_ )
        }

        inner()
    }
}
