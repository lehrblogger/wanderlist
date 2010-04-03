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
    object googleId extends MappedPoliteString(this, 256)
    object owner extends MappedLongForeignKey(this, User)
    object lastUpdated extends MappedDateTime(this)
    object contexts extends HasManyThrough(this, Group, ContactGroup, ContactGroup.contact, ContactGroup.context)
}
object Contact extends Contact with LongKeyedMetaMapper[Contact] {}

class ContactGroup extends LongKeyedMapper[ContactGroup] with IdPK {
    def getSingleton = ContactGroup
    object contact extends MappedLongForeignKey(this, Contact)
    object context extends MappedLongForeignKey(this, Group)
}
object ContactGroup extends ContactGroup with LongKeyedMetaMapper[ContactGroup] {
    def join (c: Contact, g: Group) = 
        //TODO validate that Contact and Group have same owner
        this.create.contact(c).context(g).save
}
