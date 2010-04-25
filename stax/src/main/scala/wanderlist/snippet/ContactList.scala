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
 
 
class ContactList {
    def getDisplayableName(contact: Contact): String = {
        for (identifierType <- List(IdentifierType.FullName, IdentifierType.Email,IdentifierType.TwitterHandle)) {
            val value = Identifier.findAll(By(Identifier.contact, contact),
                                           By(Identifier.idType, identifierType)).head.value
            if (value != "") return value
        }
        return ""
    }
    
    def listContacts(xhtml: NodeSeq) = { 
        Contact.findAll(By(Contact.owner, User.currentUser.open_!)).flatMap(
            contact => bind("contact", xhtml, 
                "name" -> getDisplayableName(contact)
            )
        )
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