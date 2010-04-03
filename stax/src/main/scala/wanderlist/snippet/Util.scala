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
    
    def hasGoogleProvider(html: NodeSeq) = 
        if (GoogleProvider.findAll(By(GoogleProvider.owner, User.currentUser.open_!)) != Nil) html else NodeSeq.Empty 
    
    def needsGoogleProvider(html: NodeSeq) = 
        if (GoogleProvider.findAll(By(GoogleProvider.owner, User.currentUser.open_!)) == Nil) html else NodeSeq.Empty

    def hasFoursquareService(html: NodeSeq) = 
        if (FoursquareService.findAll(By(FoursquareService.owner, User.currentUser.open_!)) != Nil) html else NodeSeq.Empty 

    def needsFoursquareService(html: NodeSeq) = 
        if (FoursquareService.findAll(By(FoursquareService.owner, User.currentUser.open_!)) == Nil) html else NodeSeq.Empty
    
    def hasHotpotatoService(html: NodeSeq) = 
        if (HotpotatoService.findAll(By(HotpotatoService.owner, User.currentUser.open_!)) != Nil) html else NodeSeq.Empty 

    def needsHotpotatoService(html: NodeSeq) = 
        if (HotpotatoService.findAll(By(HotpotatoService.owner, User.currentUser.open_!)) == Nil) html else NodeSeq.Empty
}