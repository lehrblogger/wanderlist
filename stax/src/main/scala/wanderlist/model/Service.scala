package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._
import _root_.net.liftweb.http._ 
import _root_.net.liftweb.util._ 
import S._ 
import Helpers._
import _root_.scala.xml.{Group => XMLGroup, _}
import dispatch._
import oauth._
import OAuth._

 
class HotpotatoService extends LongKeyedMapper[HotpotatoService] with IdPK { 
  def getSingleton = HotpotatoService 
  object name extends MappedPoliteString(this, 256)
  object owner extends MappedLongForeignKey(this, User)
}
object HotpotatoService extends HotpotatoService with LongKeyedMetaMapper[HotpotatoService] {}

class HotpotatoServiceContact extends LongKeyedMapper[HotpotatoServiceContact] with IdPK {
    def getSingleton = HotpotatoServiceContact
    object service extends MappedLongForeignKey(this, HotpotatoService)
    object contact extends MappedLongForeignKey(this, Contact)
}
object HotpotatoServiceContact extends HotpotatoServiceContact with LongKeyedMetaMapper[HotpotatoServiceContact] {
    def join (s: HotpotatoService, c: Contact) = 
        this.create.service(s).contact(c).save
}

class FoursquareService extends LongKeyedMapper[FoursquareService] with IdPK { 
    def getSingleton = FoursquareService 
    object name extends MappedPoliteString(this, 256)
    object authenticated extends MappedBoolean(this)
    object owner extends MappedLongForeignKey(this, User)
    object accessTokenKey extends MappedString(this, 256)
    object accessTokenSecret extends MappedString(this, 256)

    val GetRequestToken = "request_token"
    val AuthorizeToken  = "authorize"
    val GetAccessToken  = "access_token"
    val account = :/("foursquare.com") / "oauth"

    val h = new Http
    val consumer = Consumer(Props.get("foursquareConsumer.key").open_!, Props.get("foursquareConsumer.secret").open_!)

    def initiateRequest() = {
        var requestToken: Token = null
        requestToken = h(account / GetRequestToken <@ consumer as_token)
        TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
        TempToken.create.owner(User.currentUser.open_!).key(requestToken.value).secret(requestToken.secret).save
        val url = (account / AuthorizeToken <<? requestToken to_uri).toString
        S.redirectTo(url) 
    }
    
    def exchangeToken(verifier: String) {
        val tempToken = TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).head
        TempToken.findAll(By(TempToken.owner, User.currentUser.open_!)).foreach(_.delete_!)
        val requestToken = new Token(tempToken.key, tempToken.secret)
        val accessToken = h(account / GetAccessToken <@ (consumer, requestToken, verifier) as_token)
        FoursquareService.create.authenticated(true).owner(User.currentUser.open_!).accessTokenKey(accessToken.value).accessTokenSecret(accessToken.secret).save                            
    }

    def getTokenForUser(user: User) = {
        val foursquare_service = FoursquareService.findAll(By(FoursquareService.owner, user)).head
        Token(foursquare_service.accessTokenKey, foursquare_service.accessTokenSecret)
    }
}
object FoursquareService extends FoursquareService with LongKeyedMetaMapper[FoursquareService] {}

class FoursquareServiceContact extends LongKeyedMapper[FoursquareServiceContact] with IdPK {
    def getSingleton = FoursquareServiceContact
    object service extends MappedLongForeignKey(this, FoursquareService)
    object contact extends MappedLongForeignKey(this, Contact)
}
object FoursquareServiceContact extends FoursquareServiceContact with LongKeyedMetaMapper[FoursquareServiceContact] {
    def join (s: FoursquareService, c: Contact) = 
        this.create.service(s).contact(c).save
}



