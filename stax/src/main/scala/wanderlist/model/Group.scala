package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._
 
class Group extends LongKeyedMapper[Group] with IdPK { 
    def getSingleton = Group 
    object name extends MappedPoliteString(this, 256)
    object value extends MappedPoliteString(this, 256)
    object service extends MappedEnum(this, AuthService)
    object owner extends MappedLongForeignKey(this, User)
    object lastUpdated extends MappedDateTime(this)
    object contacts extends HasManyThrough(this, Contact, ContactGroup, ContactGroup.group, ContactGroup.contact) 
}
object Group extends Group with LongKeyedMetaMapper[Group]