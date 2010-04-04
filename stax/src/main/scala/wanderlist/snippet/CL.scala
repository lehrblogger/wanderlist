package wanderlist.snippet 
import wanderlist._ 
//import wanderlist.lib._
import model._ 
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
        val gp = new GoogleProvider
        val token = gp.getTokenForUser(User.currentUser.open_!)
        bind("cl", xhtml, "name" -> gp.getUserId(token) )
    }   
    
    def listGroups(xhtml: NodeSeq) = { 
        val groups = Group.findAll(By(Group.owner, User.currentUser.open_!))
        groups.flatMap(group => bind("cl", xhtml, "name" -> group.name))
    }
    
    def listContacts(xhtml: NodeSeq) = { 
        Contact.findAll(By(Contact.owner, User.currentUser.open_!), MaxRows(30)).flatMap(
            contact => bind("cl", xhtml, 
                "name"   -> contact.name,
                "emails" -> ContactEmail.findAll(By(ContactEmail.contact, contact)).flatMap(
                    contactemail => bind("e", chooseTemplate("email", "list", xhtml), 
                        "address" -> contactemail.email.toString
                    )
                )
            )
        )
    }

  //  
  // private def doList(reDraw: () => JsCmd)(html: NodeSeq): NodeSeq = 
  // toShow. 
  // flatMap(td => 
  // bind("todo", html, 
  //    "check"    -> ajaxCheckbox(td.done,                                     v => {td.done(v).save;           reDraw()}), 
  //    "priority" -> ajaxSelect(ToDo.priorityList, Full(td.priority.toString), v => {td.priority(v.toInt).save; reDraw()}), 
  //    "desc"     -> desc(td, reDraw) 
  // ))
  // 
  // 
  // private def toShow = 
  //  ToDo.findAll(By(ToDo.owner, User.currentUser), 
  //            if (QueryNotDone) By(ToDo.done, false) 
  //            else Ignore[ToDo], 
  //            OrderBy(ToDo.done, Ascending), 
  //            OrderBy(ToDo.priority, Descending), 
  //            OrderBy(ToDo.desc, Ascending))
  //            
  //    private def desc(td: ToDo, reDraw: () => JsCmd) = 
  //     swappable(<span>{td.desc}</span>, 
  //             <span>{ajaxText(td.desc, 
  //                         v => {td.desc(v).save; reDraw()})} 
  //             </span>)
  //    
  // 
  //    def list(html: NodeSeq) = { 
  //     val id = S.attr("all_id").open_! 
  // 
  //     def inner(): NodeSeq = { 
  //       def reDraw() = SetHtml(id, inner()) 
  // 
  //       bind("todo", html, 
  //          "exclude" -> ajaxCheckbox(QueryNotDone, v => {QueryNotDone(v); reDraw}), 
  //          "list"    -> doList(reDraw) _) 
  //     } 
  // 
  //     inner() 
  //    }
  //    
     
}
// 
// object QueryNotDone extends SessionVar(false)