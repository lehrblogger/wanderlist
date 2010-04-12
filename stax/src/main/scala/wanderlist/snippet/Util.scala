package wanderlist.snippet 
import net.liftweb._ 
import mapper._
import scala.xml.{NodeSeq} 
import wanderlist._ 
import wanderlist.model._ 
 
class Util { 
    def in(html: NodeSeq) = 
        if (User.loggedIn_?) html else NodeSeq.Empty 
 
    def out(html: NodeSeq) = 
        if (!User.loggedIn_?) html else NodeSeq.Empty 
}