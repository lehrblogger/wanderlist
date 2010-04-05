package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._

class ContactGroup extends LongKeyedMapper[ContactGroup] with IdPK {
    def getSingleton = ContactGroup
    object contact extends MappedLongForeignKey(this, Contact)
    object group extends MappedLongForeignKey(this, Group)
}
object ContactGroup extends ContactGroup with LongKeyedMetaMapper[ContactGroup] {
    def join (c: Contact, g: Group) = 
        //TODO validate that Contact and Group have same owner
        this.create.contact(c).group(g).save
}
