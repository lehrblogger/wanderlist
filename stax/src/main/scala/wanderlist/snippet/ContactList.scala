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
            val value = Identifier.findAll(By(Identifier.contact, contact),
                                           By(Identifier.idType, identifierType)).head.value
            if (value != "") return value
        }
        return ""
    }
    
    def getNameCloseLink(contact: Contact, linkId: String): scala.xml.Elem = {
        a(() => {
            println("click close for " + getDisplayableName(contact))
            SetHtml(linkId, getNameOpenLink(contact, linkId))
		}, Text(getDisplayableName(contact) + " close"), ("id", linkId))
	}
	
    def getNameOpenLink(contact: Contact, linkId: String): scala.xml.Elem = {
        a(() => {
            println("click open for " + getDisplayableName(contact))
            SetHtml(linkId, getNameCloseLink(contact, linkId))
		}, Text(getDisplayableName(contact) + " open"), ("id", linkId))
    }
    
    def listContacts(xhtml: NodeSeq) = { 
        Contact.findAll(By(Contact.owner, User.currentUser.open_!)).flatMap(contact => {
            val linkId = Helpers.nextFuncName
            bind("contact", xhtml, 
                "name" -> getNameOpenLink(contact, linkId)
            )
        })
    }
}
// class ContactList { 
//     def listContacts(xhtml: NodeSeq) = { 
//         Contact.findAll(By(Contact.owner, User.currentUser.open_!), MaxRows(30)).flatMap(
//             contact => bind("contactList", xhtml, 
//                 "name"   -> Identifier.findAll(By(Identifier.contact, contact),
//                                                By(Identifier.idType, IdentifierType.FullName)).head.value,
//                 "identifiers" -> Identifier.findAll(By(Identifier.contact, contact)).flatMap(
//                     identifier => bind("i", chooseTemplate("identifier", "list", xhtml), 
//                         "value" -> identifier.value
//                     )
//                 )
//             )
//         )
//     }
// }
