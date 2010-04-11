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
        if (Account.findAll(By(Account.owner, User.currentUser.open_!), 
                              By(Account.provider, AuthService.Google)) != Nil) 
            html 
        else 
            NodeSeq.Empty 
    def needsGoogleService(html: NodeSeq) = 
        if (Account.findAll(By(Account.owner, User.currentUser.open_!), 
                              By(Account.provider, AuthService.Google)) == Nil) 
            html 
        else 
            NodeSeq.Empty
        
    def hasFoursquareService(html: NodeSeq) = 
        if (Account.findAll(By(Account.owner, User.currentUser.open_!), 
                              By(Account.provider, AuthService.Foursquare)) != Nil) 
            html 
        else 
            NodeSeq.Empty 
    def needsFoursquareService(html: NodeSeq) = 
        if (Account.findAll(By(Account.owner, User.currentUser.open_!), 
                              By(Account.provider, AuthService.Foursquare)) == Nil) 
            html 
        else 
            NodeSeq.Empty

    def hasTwitterService(html: NodeSeq) = 
        if (Account.findAll(By(Account.owner, User.currentUser.open_!), 
                              By(Account.provider, AuthService.Twitter)) != Nil) 
            html 
        else 
            NodeSeq.Empty 
    def needsTwitterService(html: NodeSeq) = 
        if (Account.findAll(By(Account.owner, User.currentUser.open_!), 
                              By(Account.provider, AuthService.Twitter)) == Nil) 
            html 
        else 
            NodeSeq.Empty
                    
    def hasVimeoService(html: NodeSeq) = 
        if (Account.findAll(By(Account.owner, User.currentUser.open_!), 
                              By(Account.provider, AuthService.Vimeo)) != Nil) 
            html 
        else 
            NodeSeq.Empty 
    def needsVimeoService(html: NodeSeq) = 
        if (Account.findAll(By(Account.owner, User.currentUser.open_!), 
                              By(Account.provider, AuthService.Vimeo)) == Nil) 
            html 
        else 
            NodeSeq.Empty
                    
    def hasHotpotatoService(html: NodeSeq) = 
        if (false) html else NodeSeq.Empty
        
    def needsHotpotatoService(html: NodeSeq) = 
        if (true) html else NodeSeq.Empty
}