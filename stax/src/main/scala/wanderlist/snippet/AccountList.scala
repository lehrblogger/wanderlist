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
 
class AccountList { 
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
            val buttonSpanId = Helpers.nextFuncName
            val contactCounterName = "account_comet_" + account.id
            val count = account.contacts.length
            bind("account", xhtml, 
                "type" -> account.service,
                "notes" -> desc(account, reDraw),
                "identifiers" -> identifiersToShow(account).flatMap(identifier => 
                    bind("ident", chooseTemplate("identifier", "list", xhtml),
                        "type"  -> identifier.idType,
                        "value" -> identifier.value
                    )
                ),
                "fetch" -> {
                        if (count == 0) {
                            <span id={buttonSpanId}>{
                                ajaxButton("fetch contacts", () => {
                                    (account.service match {
                                        case Service.Foursquare => FoursquareService
                                        case Service.Google     => GoogleService
                                        case Service.Twitter    => TwitterService
                                    }).getAccountData(account, contactCounterName) 
                                    SetHtml(buttonSpanId, Text("")) & SetHtml(contactCounterName, Text("Fetching your contacts..."))
                				})
        				    }</span> 
        				} else {
                            <span id={buttonSpanId}></span>
        				}
    				},
			    "status" -> {
			            println("re-binding the status with name = " + contactCounterName + " and count = " + count + " and account.contacts.length = " + account.contacts.length)
                        <lift:comet type="ContactCounter" name={contactCounterName}>
    			                <span id={contactCounterName}>{count} contacts fetched.</span>
                        </lift:comet>
                        // if (count == 0) {
                        //                             <lift:comet type="ContactCounter" name={contactCounterName}>
                        //                                  <span id={contactCounterName}>click to get your contacts for this account</span>
                        //                             </lift:comet>
                        //                      } else { //TODO fix this since it's still a little buggy, but good enough
                        //                             <lift:comet type="ContactCounter" name={contactCounterName}>
                        //                                  <span id={contactCounterName}>{count} contacts fetched.</span>
                        //                             </lift:comet>
                        //                      }
        			}
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
