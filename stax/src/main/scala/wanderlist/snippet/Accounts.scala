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
import scala.xml.{NodeSeq, Text, UnprefixedAttribute, Null} 
import dispatch.oauth._
 
class Accounts { 
    private def desc(account: Account, reDraw: () => JsCmd) = 
        swappable(<span>{account.notes}</span>, <span>{ajaxText(account.notes, v => {account.notes(v).save; reDraw()})}</span>)

    private def identifiersToShow(account: Account) = {
        val selfContact = account.owner.obj.open_!.selfContact.obj.open_!
        Identifier.findAll(By(Identifier.contact, selfContact)).filter(identifier => {
            IdentifierAccount.findAll(By(IdentifierAccount.identifier, identifier), By(IdentifierAccount.account, account)) match {
                case List(identifierAccount) => true
                case _      => false
            }
        })
    }
        
    private def doList(reDraw: () => JsCmd)(xhtml: NodeSeq): NodeSeq = {
        Account.findAll(By(Account.owner, User.currentUser.open_!)).flatMap(account => {
            val uniqueId = Helpers.nextFuncName
            bind("account", xhtml, 
                "type" -> account.service,
                "identifiers" -> identifiersToShow(account).flatMap(identifier => 
                    bind("ident", chooseTemplate("identifier", "list", xhtml),
                        "type"  -> identifier.idType,
                        "value" -> identifier.value
                    )
                ),
                "notes" -> desc(account, reDraw),
                "status" -> <span id={uniqueId}>{
                        ajaxButton("fetch contacts", () => {
                            (account.service match {
                                case Service.Foursquare => FoursquareService
                                case Service.Google     => GoogleService
                                case Service.Twitter    => TwitterService
                            }).getContacts(Token(account.accessTokenValue, account.accessTokenSecret), uniqueId ) 
                            SetHtml(uniqueId, 
                                <lift:comet type="ContactCounter">
    					            Status: <fetchStatus:status>Missing Fetch Status</fetchStatus:status> 
    				            </lift:comet>
    				        )
        				})
    				}</span>
            )
        })
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