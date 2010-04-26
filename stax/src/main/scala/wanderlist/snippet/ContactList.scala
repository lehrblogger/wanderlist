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
import scala.xml.{NodeSeq, Elem, Text} 
 
 
class ContactList {
    def getDisplayableName(contact: Contact): String = {
        for (identifierType <- List(IdentifierType.FullName, IdentifierType.Email,IdentifierType.TwitterHandle)) {
            val identifiers = Identifier.findAll(By(Identifier.contact, contact),
                                                 By(Identifier.idType, identifierType))
            if (identifiers.length > 0) { //TODO fix this to use pattern matching
                val value = identifiers.head.value
                if (value != "") return value
            }
        }
        return ""
    }
    
    def getContactInfo(contact: Contact) = {
        println(contact.id)
        <lift:embed what="/_contact_info" contact_id={contact.id.toString}/>
    }
    
    def getNameCloseLink(contact: Contact, linkId: String, infoId: String): scala.xml.Elem = a(() => {
            SetHtml(linkId, getNameOpenLink(contact, linkId, infoId))  & SetHtml(infoId, Text(""))
		}, Text(getDisplayableName(contact)))
	
    def getNameOpenLink(contact: Contact, linkId: String, infoId: String): scala.xml.Elem = a(() => {
            SetHtml(linkId, getNameCloseLink(contact, linkId, infoId)) & SetHtml(infoId, getContactInfo(contact))
		}, Text(getDisplayableName(contact)))
    
    def list(xhtml: NodeSeq) = { 
        Contact.findAll(By(Contact.owner, User.currentUser.open_!)).filter{
            contact => (contact != User.currentUser.open_!.selfContact.obj.open_!)
        }.flatMap(contact => {
            val linkId = Helpers.nextFuncName
            val infoId = Helpers.nextFuncName
            bind("contact", xhtml, 
                "name" -> <span id={linkId}>{getNameOpenLink(contact, linkId, infoId)}</span>,
                "info" -> <div id={infoId}></div>
            )
        })
    }
    
    def info(xhtml: NodeSeq) = {
        val contact = Contact.findAll(By(Contact.id   , S.attr("contact_id").open_!.toInt ),
                                      By(Contact.owner, User.currentUser.open_!           )).head
        bind("contact", xhtml, 
            "identifiers" -> contact.identifiers.flatMap(identifier => 
                bind("identifier", chooseTemplate("identifiers", "list", xhtml), 
                    "type"    -> identifier.idType,
                    "value"   -> identifier.value,
                    "sources" -> identifier.accounts.flatMap(account => 
                        bind("source", chooseTemplate("sources", "list", xhtml), 
                            "type"    -> account.service
                        )
                    )
                )
            ),
            "groupsList" -> contact.groups.flatMap(group => 
                bind("group", chooseTemplate("groups", "list", xhtml), 
                    "name"          -> group.name,
                    "account_type"  -> group.account.obj.open_!.service
                )
            )
        )
    }
}