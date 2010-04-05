package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._

class Contact extends LongKeyedMapper[Contact] with IdPK { 
    def getSingleton = Contact 
    object name extends MappedPoliteString(this, 256)
    object owner extends MappedLongForeignKey(this, User)
    object lastUpdated extends MappedDateTime(this)
    object groups extends HasManyThrough(this, Group, ContactGroup, ContactGroup.contact, ContactGroup.group)
}
object Contact extends Contact with LongKeyedMetaMapper[Contact] {}
