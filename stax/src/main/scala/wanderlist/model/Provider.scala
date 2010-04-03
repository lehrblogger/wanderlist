package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._
 
class Provider extends LongKeyedMapper[Provider] with IdPK { 
  def getSingleton = Provider 
  object authenticated extends MappedBoolean(this) 
  object owner extends MappedLongForeignKey(this, User)
  object accessTokenKey extends MappedString(this, 256)
  object accessTokenSecret extends MappedString(this, 256)
}
object Provider extends Provider with LongKeyedMetaMapper[Provider] {}