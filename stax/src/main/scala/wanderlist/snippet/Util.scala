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
    
    def hasGoogleService(html: NodeSeq) = 
        if (AuthToken.findAll(By(AuthToken.owner, User.currentUser.open_!), 
                              By(AuthToken.provider, AuthService.Google)) != Nil) 
            html 
        else 
            NodeSeq.Empty 
        
    def needsGoogleService(html: NodeSeq) = 
        if (AuthToken.findAll(By(AuthToken.owner, User.currentUser.open_!), 
                              By(AuthToken.provider, AuthService.Google)) == Nil) 
            html 
        else 
            NodeSeq.Empty
        
    def hasFoursquareService(html: NodeSeq) = 
        if (AuthToken.findAll(By(AuthToken.owner, User.currentUser.open_!), 
                              By(AuthToken.provider, AuthService.Foursquare)) != Nil) 
            html 
        else 
            NodeSeq.Empty 

    def needsFoursquareService(html: NodeSeq) = 
        if (AuthToken.findAll(By(AuthToken.owner, User.currentUser.open_!), 
                              By(AuthToken.provider, AuthService.Foursquare)) == Nil) 
            html 
        else 
            NodeSeq.Empty

    
    def hasHotpotatoService(html: NodeSeq) = 
        if (false) html else NodeSeq.Empty
        
    def needsHotpotatoService(html: NodeSeq) = 
        if (true) html else NodeSeq.Empty
}