package wanderlist.snippet 


import net.liftweb._ 
import mapper._

import scala.xml.{NodeSeq} 
import wanderlist._ 
import model._ 
 
class Util { 
    def in(html: NodeSeq) = 
        if (User.loggedIn_?) html else NodeSeq.Empty 
 
    def out(html: NodeSeq) = 
        if (!User.loggedIn_?) html else NodeSeq.Empty 
    
    def hasCp(html: NodeSeq) = 
        if (ContactProvider.findAll(By(ContactProvider.owner, User.currentUser.open_!)) != Nil) html else NodeSeq.Empty 
    
    def needsCp(html: NodeSeq) = 
        if (ContactProvider.findAll(By(ContactProvider.owner, User.currentUser.open_!)) == Nil) html else NodeSeq.Empty
}
