package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._

// also have ConctactHandle later on
class ContactEmail extends LongKeyedMapper[ContactEmail] with IdPK { 
  def getSingleton = ContactEmail 

  object email extends MappedEmail(this) 
  object contact extends MappedLongForeignKey(this, Contact)
}
object ContactEmail extends ContactEmail with LongKeyedMetaMapper[ContactEmail] {}