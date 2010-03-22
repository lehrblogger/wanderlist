package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._
 
class Context extends LongKeyedMapper[Context] with IdPK { 
  def getSingleton = Context 
  object name extends MappedPoliteString(this, 256)
  object googleId extends MappedPoliteString(this, 256)
  object owner extends MappedLongForeignKey(this, User)
  object lastUpdated extends MappedDateTime(this)
  object contexts extends HasManyThrough(this, Context, ContactContext, ContactContext.contact, ContactContext.context) 
}
object Context extends Context with LongKeyedMetaMapper[Context] {}