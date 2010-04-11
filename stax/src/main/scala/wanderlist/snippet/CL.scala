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
 
class CL { 
    def name(xhtml: NodeSeq) = { 
        //val token = GoogleService.getAccessTokenForUser(User.currentUser.open_!)
        bind("cl", xhtml, "name" -> "fix this later")//GoogleService.getUserId(token))
    }   
    
    def listGroups(xhtml: NodeSeq) = { 
        val groups = Group.findAll(By(Group.owner, User.currentUser.open_!))
        groups.flatMap(group => bind("cl", xhtml, "name" -> group.name))
    }
    
    def listContacts(xhtml: NodeSeq) = { 
        Contact.findAll(By(Contact.owner, User.currentUser.open_!), MaxRows(30)).flatMap(
            contact => bind("cl", xhtml, 
                "name"   -> Identifier.findAll(By(Identifier.contact, contact),
                                               By(Identifier.idType, IdentifierType.FullName)).head.value,
                "identifiers" -> Identifier.findAll(By(Identifier.contact, contact),
                                               By(Identifier.owner, User.currentUser.open_!)).flatMap(
                    identifier => bind("i", chooseTemplate("identifier", "list", xhtml), 
                        "value" -> identifier.value
                    )
                )
            )
        )
    }
}