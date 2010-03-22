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
    object contexts extends HasManyThrough(this, Context, ContactContext, ContactContext.contact, ContactContext.context)
}
object Contact extends Contact with LongKeyedMetaMapper[Contact] {}

class ContactContext extends LongKeyedMapper[ContactContext] with IdPK {
    def getSingleton = ContactContext
    object contact extends MappedLongForeignKey(this, Contact)
    object context extends MappedLongForeignKey(this, Context)
}
object ContactContext extends ContactContext with LongKeyedMetaMapper[ContactContext] {
    def join (c: Contact, g: Context) = 
        this.create.contact(c).context(g).save
}
