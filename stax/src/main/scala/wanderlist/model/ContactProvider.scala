package wanderlist.model 
 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
 
class ContactProvider extends LongKeyedMapper[ContactProvider] with IdPK { 
  def getSingleton = ContactProvider 

  object authenticated extends MappedBoolean(this) 
  object owner extends MappedLongForeignKey(this, User)
  object accessTokenKey extends MappedString(this, 256)
  object accessTokenSecret extends MappedString(this, 256)
}

object ContactProvider extends ContactProvider with LongKeyedMetaMapper[ContactProvider] {}