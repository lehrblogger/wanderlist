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
 
 
class GroupList { 
    def listGroups(xhtml: NodeSeq) = { 
        val groups = Group.findAll(By(Group.owner, User.currentUser.open_!))
        groups.flatMap(group => bind("groupList", xhtml, "name" -> group.name))
    }
}