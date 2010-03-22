package wanderlist.snippet 
 
import wanderlist._ 
import wanderlist.lib._
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
        val g = new GoogleAuth
        val token = g.getTokenForUser(User.currentUser.open_!)
        bind("cl", xhtml, "name" -> g.getUserId(token) )
    }   
    
    

    
    def list(xhtml: NodeSeq) = { 
        val g = new GoogleAuth
        val token = g.getTokenForUser(User.currentUser.open_!)
        
        val tenContacts = g.getTenContacts(token)
        
        tenContacts.flatMap(contact => bind("cl", xhtml, "name"   -> contact.name,
                                                         "emails" -> contact.emails.toString))
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