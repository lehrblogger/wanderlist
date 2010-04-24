package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._

class Contact extends LongKeyedMapper[Contact] with IdPK { 
    def getSingleton = Contact 
    object owner extends MappedLongForeignKey(this, User)
}
object Contact extends Contact with LongKeyedMetaMapper[Contact] {
    def groups = ContactGroup.findAll(By(ContactGroup.contact, this.id)).map(_.group.obj.open_!)
}
